import sys
from groq import Groq

# Initialisation du client Groq
GROQ_API_KEY = "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"
client = Groq(api_key=GROQ_API_KEY)

def get_response(user_message):
    try:
        groq_response = client.chat.completions.create(
            messages=[{"role": "user", "content": user_message}],
            model="llama-3.3-70b-versatile",
        )
        return groq_response.choices[0].message.content
    except Exception as e:
        return f"Error: {e}"

# Prendre un message et répondre
user_input = input()  # L'utilisateur envoie un message
reply = get_response(user_input)
print(reply)  # La réponse est renvoyée au processus Java
sys.stdout.flush()
