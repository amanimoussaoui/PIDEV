<?php

namespace App\Form;

use App\Entity\Activite;
use App\Entity\Culture;
use App\Entity\Parcelle;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\DateType;

class ActiviteType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('description')
            ->add('type', ChoiceType::class, [
                'choices' => [
                    'Semis' => 'Semis',
                    'Arrosage' => 'Arrosage',
                    'Fertilisation' => 'Fertilisation',
                    'Récolte' => 'Récolte',
                    'Traitement' => 'Traitement',
                    'Désherbage' => 'Désherbage',
                ],
                'expanded' => false,
                'multiple' => false,
                'attr' => ['class' => 'form-control'],
            ])
            ->add('date', DateType::class, [
                'widget' => 'single_text',
                'required' => true,
                'data' => new \DateTime(),
                'attr' => ['class' => 'form-control'],
            ])
            ->add('parcelle', EntityType::class, [
                'class' => Parcelle::class,
                'choice_label' => 'nom',
                'attr' => ['class' => 'form-control'],
            ])
            ->add('culture', EntityType::class, [
                'class' => Culture::class,
                'choice_label' => 'nomCulture',
                'attr' => ['class' => 'form-control'],
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Activite::class,
        ]);
    }
}
