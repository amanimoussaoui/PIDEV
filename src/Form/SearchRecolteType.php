<?php

namespace App\Form;

use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\SearchType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class SearchRecolteType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('search', SearchType::class, [
                'required' => false,
                'attr' => [
                    'placeholder' => 'Rechercher par nom de culture...',
                ],
            ])
            ->add('qualite', ChoiceType::class, [
                'required' => false,
                'label' => false,
                'choices' => [
                    'Excellente' => 'Excellente',
                    'Bonne' => 'Bonne',
                    'Moyenne' => 'Moyenne',
                    'Médiocre' => 'Médiocre',
                    'Mauvaise' => 'Mauvaise',
                ],
                'placeholder' => 'Qualité',
            ])
          ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'csrf_protection' => false,
        ]);
    }
}