<?php

namespace App\Form;

use App\Entity\Recolte;
use App\Entity\Culture;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;

class RecolteType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('dateRecolte', DateType::class, [
                'label' => 'Date de récolte',
                'widget' => 'single_text',
                'required' => true,
                'data' => new \DateTime(),
                'attr' => ['class' => 'form-control'],
            ])
            ->add('quantite', NumberType::class, [
                'label' => 'Quantité (kg)',
                'attr' => ['min' => 0, 'class' => 'form-control'],
                'required' => true,
            ])
            ->add('qualite', ChoiceType::class, [
                'label' => 'Qualité',
                'choices' => [
                    'Excellente' => 'Excellente',
                    'Bonne' => 'Bonne',
                    'Moyenne' => 'Moyenne',
                    'Médiocre' => 'Médiocre',
                    'Mauvaise' => 'Mauvaise'
                ],
                'expanded' => false,
                'multiple' => false,
                'attr' => ['class' => 'form-control'],
                'required' => true,
            ])
            ->add('prixUnitaire', NumberType::class, [
                'label' => 'Prix Unitaire (€)',
                'attr' => ['min' => 0, 'class' => 'form-control'],
                'required' => true,
            ])
            ->add('culture', EntityType::class, [
                'class' => Culture::class,
                'choice_label' => 'nomCulture',
                'attr' => ['class' => 'form-control'],
                'required' => true,
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Recolte::class,
        ]);
    }
}