<?php

namespace App\Form;

use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\SearchType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class SearchActiviteType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('search', SearchType::class, [
                'label' => 'Recherche par culture',
                'required' => false,
                'attr' => ['placeholder' => 'Nom de la culture...'],
            ])
            ->add('type', ChoiceType::class, [
                'label' => 'Filtrer par type d\'activité',
                'choices' => [
                      'Semis' => 'Semis',
                        'Plantation' => 'Plantation',
                        'Arrosage' => 'Arrosage',
                        'Fertilisation' => 'Fertilisation',
                        'Traitement phytosanitaire' => 'Traitement phytosanitaire',
                        'Récolte' => 'Récolte',
                        'Élagage / Taille' => 'Élagage / Taille',
                        'Greffage' => 'Greffage',
                ],
                'required' => false,
                'placeholder' => 'Type Activité',
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([]);
    }
}

