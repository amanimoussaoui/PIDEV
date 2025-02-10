<?php

namespace App\Form;

use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class SearchParcelleType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nom', TextType::class, [
                'label' => 'Rechercher par nom',
                'required' => false,
                'attr' => [
                    'placeholder' => 'Entrez le nom de la parcelle',
                ],
            ])
            ->add('typeSol', ChoiceType::class, [
                'label' => 'Filtrer par type de sol',
                'choices' => [
                    'Tous' => null,
                    'Argileux' => 'argileux',
                    'Sableux' => 'sableux',
                    'Limoneux' => 'limoneux',
                    'Humifère' => 'humifère',
                ],
                'required' => false,
                'placeholder' => 'Choisissez un type de sol',
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'csrf_protection' => false,
        ]);
    }
}