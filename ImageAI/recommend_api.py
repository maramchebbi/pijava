from flask import Flask, request, jsonify
import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import mysql.connector
import logging

# Configuration du logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

app = Flask(__name__)

def get_data_from_db():
    try:
        conn = mysql.connector.connect(
            host="localhost",
            user="root",
            password="",
            database="projetsymfony"
        )

        # Récupérer seulement titre et localisation
        event = pd.read_sql("SELECT id, titre, localisation FROM event", conn)
        logger.info(f"Récupéré {len(event)} événements de la base de données")

        # Récupérer les participations
        participation = pd.read_sql("SELECT user_id, event_id FROM participation", conn)
        logger.info(f"Récupéré {len(participation)} participations de la base de données")

        conn.close()
        return event, participation
    except Exception as e:
        logger.error(f"Erreur lors de la récupération des données: {e}")
        # Créer des DataFrames vides en cas d'erreur
        event = pd.DataFrame(columns=["id", "titre", "localisation"])
        participation = pd.DataFrame(columns=["user_id", "event_id"])
        return event, participation

def create_event_features(event_df):
    """Crée des caractéristiques textuelles pour chaque événement"""
    # Remplacer les valeurs nulles par des chaînes vides
    event_df['localisation'] = event_df['localisation'].fillna('')

    # Créer une caractéristique textuelle combinant titre et localisation
    event_df['text_features'] = event_df.apply(
        lambda row: f"{row['titre']} {row['localisation']}",
        axis=1
    )

    return event_df

def get_user_profile(user_id, event_df, participation_df):
    """Créer un profil utilisateur basé sur ses participations passées"""
    # Obtenir les événements auxquels l'utilisateur a participé
    user_event_ids = participation_df[participation_df['user_id'] == user_id]['event_id'].tolist()
    logger.info(f"Utilisateur {user_id} a participé à {len(user_event_ids)} événements")

    if not user_event_ids:
        return None, user_event_ids

    user_events = event_df[event_df['id'].isin(user_event_ids)]

    if user_events.empty:
        return None, user_event_ids

    # Créer un profil en combinant les caractéristiques de tous les événements
    user_profile = " ".join(user_events['text_features'].tolist())

    return user_profile, user_event_ids

def content_based_recommendations(user_id, event_df, participation_df):
    """Générer des recommandations basées sur le contenu"""
    # Créer les caractéristiques des événements
    event_df = create_event_features(event_df)

    # Obtenir le profil utilisateur
    user_profile, user_event_ids = get_user_profile(user_id, event_df, participation_df)

    # Si l'utilisateur n'a pas de profil, retourner les événements les plus populaires
    if user_profile is None:
        logger.info(f"Pas de profil pour l'utilisateur {user_id}, utilisation des événements populaires")
        return popular_event_recommendations(event_df, participation_df, user_event_ids)

    try:
        # Créer une matrice TF-IDF pour les événements
        tfidf = TfidfVectorizer(min_df=0.0)
        tfidf_matrix = tfidf.fit_transform(event_df['text_features'])

        # Transformer le profil utilisateur en vecteur TF-IDF
        user_vector = tfidf.transform([user_profile])

        # Calculer la similarité entre le profil utilisateur et tous les événements
        cosine_similarities = cosine_similarity(user_vector, tfidf_matrix).flatten()

        # Ajouter les scores de similarité au DataFrame
        event_df['similarity_score'] = cosine_similarities

        # Si tous les scores sont proches de zéro, ajouter un petit bruit pour différencier
        if np.all(cosine_similarities < 0.01):
            logger.warning("Tous les scores de similarité sont proches de zéro, ajout de bruit")
            noise = np.random.uniform(0.1, 0.5, size=len(cosine_similarities))
            event_df['similarity_score'] = event_df['similarity_score'] + noise

        # Filtrer les événements auxquels l'utilisateur a déjà participé
        unseen_events = event_df[~event_df['id'].isin(user_event_ids)]

        if unseen_events.empty:
            logger.warning(f"L'utilisateur {user_id} a participé à tous les événements disponibles")
            return []

        # Trier par score de similarité décroissant
        recommendations = unseen_events.sort_values('similarity_score', ascending=False)

        # Préparer le résultat
        result = recommendations[['id', 'titre', 'similarity_score']].head(5).to_dict(orient='records')

        return result

    except Exception as e:
        logger.error(f"Erreur lors du calcul des recommandations basées sur le contenu: {e}")
        # En cas d'erreur, utiliser les recommandations populaires
        return popular_event_recommendations(event_df, participation_df, user_event_ids)

def popular_event_recommendations(event_df, participation_df, user_event_ids):
    """Générer des recommandations basées sur la popularité"""
    try:
        # Calculer la popularité de chaque événement (nombre de participations)
        event_popularity = participation_df.groupby('event_id').size().reset_index(name='popularity')

        # Fusionner avec le DataFrame des événements
        event_with_popularity = pd.merge(event_df, event_popularity, left_on='id', right_on='event_id', how='left')
        event_with_popularity['popularity'] = event_with_popularity['popularity'].fillna(0)

        # Normaliser la popularité pour obtenir un score entre 0 et 1
        max_popularity = event_with_popularity['popularity'].max()
        if max_popularity > 0:
            event_with_popularity['similarity_score'] = event_with_popularity['popularity'] / max_popularity
        else:
            # Si pas de données de popularité, utiliser un score aléatoire entre 0.1 et 0.5
            event_with_popularity['similarity_score'] = np.random.uniform(0.1, 0.5, size=len(event_with_popularity))

        # Filtrer les événements auxquels l'utilisateur a déjà participé
        unseen_events = event_with_popularity[~event_with_popularity['id'].isin(user_event_ids)]

        if unseen_events.empty:
            # Si l'utilisateur a déjà participé à tous les événements, retourner liste vide
            return []

        # Trier par score décroissant
        recommendations = unseen_events.sort_values('similarity_score', ascending=False)

        # Préparer le résultat
        result = recommendations[['id', 'titre', 'similarity_score']].head(5).to_dict(orient='records')

        return result

    except Exception as e:
        logger.error(f"Erreur lors du calcul des recommandations populaires: {e}")
        # En dernier recours, retourner des événements aléatoires
        return random_recommendations(event_df, user_event_ids)

def random_recommendations(event_df, user_event_ids):
    """Générer des recommandations aléatoires"""
    # Filtrer les événements auxquels l'utilisateur a déjà participé
    unseen_events = event_df[~event_df['id'].isin(user_event_ids)]

    if unseen_events.empty:
        return []

    # Sélectionner jusqu'à 5 événements aléatoires
    random_events = unseen_events.sample(min(5, len(unseen_events)))

    # Ajouter des scores aléatoires entre 0.1 et 0.9
    random_events['similarity_score'] = np.random.uniform(0.1, 0.9, size=len(random_events))

    # Préparer le résultat
    result = random_events[['id', 'titre', 'similarity_score']].to_dict(orient='records')

    return result

@app.route("/recommend", methods=["GET"])
def recommend():
    user_id = int(request.args.get("user_id"))
    logger.info(f"Générer des recommandations pour user_id: {user_id}")

    # Récupérer les données
    event, participation = get_data_from_db()

    if event.empty:
        logger.warning("Aucun événement trouvé dans la base de données")
        return jsonify([])

    # Essayer de générer des recommandations basées sur le contenu
    recommendations = content_based_recommendations(user_id, event, participation)

    logger.info(f"Recommandations générées: {recommendations}")
    return jsonify(recommendations)

if __name__ == "__main__":
    app.run(port=5003, debug=True)
    logger.info("API démarrée sur http://127.0.0.1:5003")