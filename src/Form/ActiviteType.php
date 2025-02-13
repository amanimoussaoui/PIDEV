<?php

namespace App\Form;

use App\Entity\Activite;
use App\Entity\Culture;
use App\Entity\Parcelle;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\FormEvent;
use Symfony\Component\Form\FormEvents;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\FormError;


class ActiviteType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
          
            ->add('description')
            ->add('date', DateType::class, [
                'widget' => 'single_text',
                'required' => true,
                'data' => new \DateTime(),
                'attr' => ['class' => 'form-control'],
            ])
            ->add('type', ChoiceType::class, [
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
                'placeholder' => 'Sélectionnez un type',
                'attr' => ['class' => 'form-control'],
                'required' => true,
            ])
          
            ->add('culture', EntityType::class, [
                'class' => Culture::class,
                'choice_label' => 'nomCulture',
                'required' => false, // Initially optional
                'attr' => ['class' => 'form-control'],
                'placeholder' => 'Sélectionnez une culture',
            ]);
    }
    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Activite::class,
        ]);
    }
}