package Models;


import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class Vote {
        private int id;

        @NotNull(message = "L'ID du textile est obligatoire")
        private int textileId;

        @NotNull(message = "L'ID de l'utilisateur est obligatoire")
        private int userId;

        @NotNull(message = "La valeur de l'évaluation est obligatoire")
        @Min(value = 1, message = "L'évaluation minimum est 1")
        @Max(value = 5, message = "L'évaluation maximum est 5")
        private int value;

        // Constructeur par défaut
        public Vote() {
        }

        // Constructeur avec tous les champs
        public Vote(int id, int textileId, int userId, int value) {
            this.id = id;
            this.textileId = textileId;
            this.userId = userId;
            this.value = value;
        }

        // Constructeur sans ID (pour l'insertion)
        public Vote(int textileId, int userId, int value) {
            this.textileId = textileId;
            this.userId = userId;
            this.value = value;
        }

        // Getters and Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getTextileId() {
            return textileId;
        }

        public void setTextileId(int textileId) {
            this.textileId = textileId;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Vote{" +
                    "id=" + id +
                    ", textileId=" + textileId +
                    ", userId=" + userId +
                    ", value=" + value +
                    '}';
        }
    }

