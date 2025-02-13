<?php

namespace App\Enum;

enum StatutCommande: string
{
    case EN_ATTENTE = 'En attente';
    case EXPEDIEE = 'Expédiée';
    case LIVREE = 'Livrée';  // Assurez-vous que cette valeur est bien définie
    case ANNULEE = 'Annulée';
     case EN_COURS = 'En cours';

}
