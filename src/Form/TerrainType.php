<?php

namespace App\Form;

use App\Entity\Terrain;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\UrlType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\Validator\Constraints\NotBlank;
use Symfony\Component\Validator\Constraints\File;

class TerrainType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
        ->add('localisation', TextType::class, [
            'label' => 'Localisation',
            'constraints' => [
              
            ]
        ])
       
            ->add('superficie', NumberType::class, [
                'label' => 'Superficie (m²)',
               'constraints' => [
                
                ]
                ])
            ->add('prix', NumberType::class, [
                'label' => 'Prix',
                'constraints' => [
                
                ]
                ])
           
    ->add('description', TextType::class, [
        'label' => 'Description du terrain',
        'constraints' => [
            
        ]
    ])
    ->add('image', FileType::class, [
        'required' => false,
        'mapped' => true, // lié à l'entité
        'data_class' => null, // désactive la validation automatique
    ])
    ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Terrain::class,
        ]);
    }
    
}
