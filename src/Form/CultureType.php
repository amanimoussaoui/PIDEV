<?php

namespace App\Form;

use App\Entity\Culture;
use App\Entity\Parcelle;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\DateType;

class CultureType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nomCulture')
            ->add('dateSemis', DateType::class, [
                'widget' => 'single_text',
                'required' => true,
                'data' => new \DateTime(), 
                'attr' => ['class' => 'form-control'],
            ])
            ->add('duree')
            ->add('statut', ChoiceType::class, [
                'choices' => Culture::getStatutlChoices(),
                'placeholder' => 'Sélectionnez un statut',
            ])
            ->add('parcelle', EntityType::class, [
                'class' => Parcelle::class,
                'choice_label' => 'nom',
                'placeholder' => 'Sélectionnez une parcelle',
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Culture::class,
        ]);
    }
}
