<?php

namespace App\Form;

use App\Entity\Machine;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\SubmitType;
use Symfony\Component\Form\Extension\Core\Type\DateTimeType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class MachineType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nom', null, [
                'attr' => ['class' => 'form-control', 'placeholder' => 'Entrez le nom de la machine'],
            ])
            ->add('description', null, [
                'attr' => ['class' => 'form-control', 'placeholder' => 'Entrez la description de la machine'],
            ])
            ->add('prix', null, [
                'attr' => ['class' => 'form-control', 'placeholder' => 'Entrez le prix de la machine'],
            ])
            ->add('etat', ChoiceType::class, [
                'choices'  => [
                    'Neuf' => 'Neuf',
                    'Utilisé' => 'Utilisé',
                ],
                'expanded' => false,  // Désactive les boutons radio
                'multiple' => false,
                'attr' => ['class' => 'form-select'], // Utilise la classe Bootstrap pour le sélecteur
            ])
            ->add('disponibilite', ChoiceType::class, [
                'choices'  => [
                    'Oui' => 'Oui',
                    'Non' => 'Non',
                ],
                'expanded' => false,  // Désactive les boutons radio
                'multiple' => false,
                'attr' => ['class' => 'form-select'], // Utilise la classe Bootstrap pour le sélecteur
            ])
            // Ajout du champ date_maintenance
            ->add('date_maintenance', DateTimeType::class, [
                'widget' => 'single_text',  // Utilisation d'un champ de texte unique pour la date
                'attr' => ['class' => 'form-control'],  // Utilisation de la classe Bootstrap pour le champ
                'required' => false,  // Le champ est facultatif, vous pouvez l'adapter si nécessaire
                'label' => 'Date de maintenance', // Label du champ
            ])
            ->add('save', SubmitType::class, [
                'attr' => ['class' => 'btn btn-success btn-block'], // Bouton avec style Bootstrap
                'label' => 'Enregistrer',
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Machine::class,
        ]);
    }
}
