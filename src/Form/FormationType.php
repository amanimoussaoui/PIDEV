<?php

namespace App\Form;

use App\Entity\Formation;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\SubmitType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\Validator\Constraints\File;
use Vich\UploaderBundle\Form\Type\VichImageType;
class FormationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('titre')
            ->add('description')
            ->add('prix')
            ->add('date', null, [
                'widget' => 'single_text',
            ])
           // ->add('image')
           ->add('file', VichImageType::class, [
            'label' => 'Image du formation',
            'required' => false,
            'allow_delete' => false,
            'download_uri' => false,
            'image_uri' => false, // Désactive l'affichage de l'URL de l'image, car on veut stocker un fichier
            'constraints' => [
                new File([
                    'maxSize' => '2M',
                    'mimeTypes' => ['image/jpeg', 'image/png', 'image/webp','image/jpg'],
                    'mimeTypesMessage' => 'Veuillez télécharger une image valide (JPG, PNG, WebP,jpg).',
                ])
            ],
            
        ])
            ->add('ajouter', SubmitType::class)
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Formation::class,
            'attr' => ['novalidate' => 'novalidate'],
        ]);
    }
}

