<?php
// src/Form/CandidatureType.php

namespace App\Form;

use App\Entity\Candidature;
use App\Entity\Terrain;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\SubmitType;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\Extension\Core\Type\HiddenType;
use Symfony\Component\Form\Extension\Core\Type\MoneyType;  // Corriger ici
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\Validator\Constraints\NotBlank;
use Symfony\Component\Validator\Constraints\File;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Validator\Constraints as Assert;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Validator\Context\ExecutionContextInterface;




class CandidatureType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options)
    {
        // Récupérer les terrains et l'id de l'utilisateur depuis les options
        $terrains = $options['terrains'];

        $builder
            
        ->add('idTerrain', EntityType::class, [
            'class' => Terrain::class,  // Classe de l'entité Terrain
            'choices' => $terrains,  // Passer les terrains depuis le contrôleur
            'choice_label' => function ($terrain) {
                // Assurez-vous de choisir un attribut spécifique de l'objet Terrain
                return $terrain->getId(); 
                 
            },
            'required' => true,
            'disabled' => true,
        ])
        ->add('montant', MoneyType::class, [
            'disabled' => true,  // Désactive ce champ
        ])
            ->add('dateDebut', DateType::class, [
                'widget' => 'single_text',
                'required' => false,
                'constraints' => [
                    new Assert\NotBlank([
                        'message' => 'La date de début ne doit pas être vide.',
                    ]),
                ],
            ])
            ->add('dateFin', DateType::class, [
                'widget' => 'single_text',
                'required' => false,
                'constraints' => [
                    new Assert\NotBlank([
                        'message' => 'La date de fin ne doit pas être vide.',
                    ]),
                ],
            ])
            
            ->add('but', TextType::class, [
                'required' => true,
                'constraints' => [
                    new Assert\NotBlank([
                        'message' => 'Le but ne doit pas être vide.',
                    ]),
                    new Assert\Length([
                        'min' => 10,
                        'minMessage' => 'Le but doit comporter au moins 10 caractères.',
                    ]),
                ],
            ]);
             
                
            ;
    }

    public function configureOptions(OptionsResolver $resolver)
    {
        $resolver->setDefaults([
            'data_class' => Candidature::class,
            'terrains' => [],
            
        ]);
    }
}
