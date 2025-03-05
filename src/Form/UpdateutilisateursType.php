<?php

namespace App\Form;

use App\Entity\Utilisateurs;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class UpdateutilisateursType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('email', EmailType::class, [
                'label' => 'Email'
            ])
            ->add('roles', ChoiceType::class, [
                'choices'  => [
                    'Client' => 'ROLE_CLIENT',
                    'Admin' => 'ROLE_ADMIN',
                    'Agriculteurs' => 'ROLE_AGRICULTEUR'
                ],
                'expanded' => true,
                'multiple' => true,
                'label' => 'Roles'
            ])
            ->add('nom', TextType::class, [
                'label' => 'Last Name'
                
            ])
            ->add('prenom', TextType::class, [
                'label' => 'First Name'
            ]);
           
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Utilisateurs::class,
        ]);
    }
}
