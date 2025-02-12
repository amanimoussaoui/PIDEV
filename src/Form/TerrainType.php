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

class TerrainType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('localisation', TextType::class, [
                'label' => 'Localisation',
                'required' => true,
                'attr' => ['placeholder' => 'Indiquez la localisation du terrain'],
            ])
            ->add('superficie', NumberType::class, [
                'label' => 'Superficie (m²)',
                'required' => true,
                'attr' => ['placeholder' => 'Indiquez la superficie du terrain'],
            ])
            ->add('prix', NumberType::class, [
                'label' => 'Prix',
                'required' => true,
                'attr' => ['placeholder' => 'Indiquez le prix du terrain'],
            ])
            ->add('description', TextareaType::class, [
                'label' => 'Description',
                'required' => true,
                'attr' => ['placeholder' => 'Ajoutez une description du terrain', 'rows' => 4],
            ])
            ->add('image', UrlType::class, [
                'label' => 'Image URL',
                'required' => false,
                'attr' => ['placeholder' => 'https://exemple.com/image.jpg'],
            ])
            ->add('username', TextType::class, [
                'label' => 'UserName',
                'required' => true,
                'attr' => ['placeholder' => 'Indiquez le nom'],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Terrain::class,
        ]);
    }
}
