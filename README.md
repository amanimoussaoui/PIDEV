# Agriwise

**Agriwise** est une application Web et Desktop développée avec **Symfony 6.4**, dédiée à la gestion intelligente de l'agriculture. Elle intègre plusieurs modules destinés à faciliter le quotidien des agriculteurs et des utilisateurs de la plateforme.

---

## 🧩 Modules Principaux

### 1. 👤 Gestion des utilisateurs
- Authentification avec **reCAPTCHA v3**.
- Capture d'écran pour vérification de session.
- Fonction "Mot de passe oublié" avec envoi d'e-mail via **Mailtrap**.
- Gestion des rôles et des autorisations.

---

### 2. 🛒 Produits & Commandes
- Gestion des produits agricoles, catégories et stocks.
- Création et gestion des paniers.
- Gestion des commandes avec notifications.
- Paiement en ligne sécurisé via **Stripe**.
- Notifications SMS via **Twilio**.
- Support de **traduction multilingue** et gestion de la **devise locale**.

---

### 3. 📚 Formations
- Liste de formations disponibles.
- Participation des utilisateurs à une formation.
- Envoi automatique d’e-mails en cas d’annulation d’une formation.

---

### 4. 🚜 Réservation de machines
- Réservation de machines agricoles.
- Visualisation des détails techniques de chaque machine.
- Génération et lecture de **QR codes** pour chaque machine.

---

### 5. 🌾 Gestion Agricole
- Gestion des **parcelles agricoles**.
- Suivi des récoltes par parcelle.
- Suivi des activités agricoles.
- **Prédiction intelligente** des récoltes ou traitements via **algorithmes Python intégrés**.

---

### 6. 📝 Gestion des candidatures
- Les agriculteurs peuvent soumettre des **terrains** disponibles.
- Les clients peuvent postuler sur ces terrains.
- Intégration de l’**IA** pour **prédire les activités agricoles possibles sur un terrain**.
- Envoi d’e-mails de confirmation lors des candidatures.

---

## ⚙️ Technologies utilisées
- **Symfony 6.4**
- **Stripe API** (paiements)
- **Twilio API** (SMS)
- **Mailtrap** (mails de test)
- **Python** (prédictions agricoles)
- **QR Code** (gestion machines)
- **Google reCAPTCHA v3**
- **Leaflet / OpenStreetMap** (intégration carte)
- **Multi-langues et multi-devise**

---

## 🚀 Objectif
Agriwise vise à améliorer la productivité, la gestion et la planification agricole grâce à des outils modernes, une interface intuitive et des technologies intelligentes au service des agriculteurs.

---

## 📦 Installation

```bash
git clone https://github.com/amanimoussaoui/PIDEV.git
cd agriwise
composer install
npm install
npm run build
php bin/console doctrine:database:create
php bin/console doctrine:migrations:migrate
symfony server:start
