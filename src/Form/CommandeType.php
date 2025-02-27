<?php

namespace App\Form;

use App\Entity\Commande;
use App\Enum\StatutCommande;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;

class CommandeType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
         
            ->add('date', DateType::class, [
                'widget' => 'single_text',
                'label' => 'Date de la commande',
            ])
            ->add('adresse', TextType::class, [
                'label' => 'Adresse de livraison',
            ])
          ;
          
            
    }
    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Commande::class,
            'attr' => ['novalidate' => 'novalidate'],
        ]);
    }
}
