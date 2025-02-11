<?php

namespace App\Form;

use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\SearchType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class SearchCultureType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('search', SearchType::class, [
                'required' => false,
                'attr' => [
                    'placeholder' => 'Rechercher par nom culture ou parcelle...',
                ],
            ])
            ->add('statut', ChoiceType::class, [
                'required' => false,
                'label' => false,
                'choices' => [
                    'En Culture' => 'en_culture',
                    'Récolte' => 'recolte',
                    'Terminé' => 'termine',
                ],
                'placeholder' => 'Statut',
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'csrf_protection' => false,
        ]);
    }
}