<?php

namespace App\Form;

use App\Entity\Commande;
use App\Enum\StatutCommande;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use App\Entity\User;

class CommandeType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('statut', ChoiceType::class, [
                'choices' => [
                    'En attente' => StatutCommande::EN_ATTENTE,
                    'Expédiée' => StatutCommande::EXPEDIEE,
                    'Livrée' => StatutCommande::LIVREE,
                    'Annulée' => StatutCommande::ANNULEE,
                ],
                'label' => 'Statut de la commande',
            ])
            ->add('date', DateType::class, [
                'widget' => 'single_text',
                'label' => 'Date de la commande',
            ])
            ->add('adresse', TextType::class, [
                'label' => 'Adresse de livraison',
            ])
            ->add('user_id', EntityType::class, [
                'class' => User::class,
                'choice_label' => 'nom',  // Affiche le nom de l'utilisateur
                'label' => 'Utilisateur',
            ]);
          
            
    }
}
