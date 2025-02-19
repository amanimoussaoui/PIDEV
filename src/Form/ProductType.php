<?php

namespace App\Form;

use App\Entity\Product;
use App\Entity\User;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\SubmitType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\Validator\Constraints\File;
use Vich\UploaderBundle\Form\Type\VichImageType;

class ProductType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nom')
            ->add('description')
            ->add('prix')
            ->add('stock')
            ->add('category', ChoiceType::class, [
                'choices'  => [
                    'Fruits' => 'fruits',
                    'Légumes' => 'legumes',
                    'Graines' => 'graines',
                    'Épices' => 'epices',
                ],
                'placeholder' => 'Choisissez une catégorie',
                'expanded' => false, // false = liste déroulante | true = boutons radio
                'multiple' => false, // false = choix unique | true = choix multiple
            ])
           
           
            ->add('file', VichImageType::class, [
                'label' => 'Image du produit',
                'required' => false,
                'allow_delete' => false,
                'download_uri' => false,
                'image_uri' => false, // Désactive l'affichage de l'URL de l'image, car on veut stocker un fichier
                'constraints' => [
                    new File([
                        'maxSize' => '2M',
                        'mimeTypes' => ['image/jpeg', 'image/png', 'image/webp'],
                        'mimeTypesMessage' => 'Veuillez télécharger une image valide (JPG, PNG, WebP).',
                    ])
                ],
                
            ])
            ->add('user', EntityType::class, [
                'class' => User::class,
                'choice_label' => 'nom', // Affiche le nom de l'utilisateur au lieu de l'ID
            ])
            ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Product::class,
            'attr' => ['novalidate' => 'novalidate'],
        ]);
    }
}
