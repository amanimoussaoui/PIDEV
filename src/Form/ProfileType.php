<?php

namespace App\Form;

use App\Entity\Profile;
use App\Entity\Utilisateurs;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\FileType;

class ProfileType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('adresse')
            ->add('num_tel')
            ->add('image', FileType::class,[
                'required' => false,
                'mapped' => true,
                'data_class' => null, // Désactive la validation du type de données
            ]
            )
            ->add('bio')
            ->add('date_de_naissance', null, [
                'widget' => 'single_text',
            ])
            ->add('prenomf')
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Profile::class,
        ]);
    }
}
