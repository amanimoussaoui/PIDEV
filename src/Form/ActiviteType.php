<?php

namespace App\Form;

use App\Entity\Activite;
use App\Entity\Culture;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Doctrine\ORM\EntityRepository;

class ActiviteType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $user = $options['user']; // Get the logged-in user from the options

        $builder
            ->add('description')
            ->add('date', DateType::class, [
                'widget' => 'single_text',
                'html5' => true,
                'attr' => ['class' => 'form-control'],
                'required' => true,
            ])
            ->add('type', ChoiceType::class, [
                'choices' => [
                    'Semis' => 'Semis',
                    'Plantation' => 'Plantation',
                    'Arrosage' => 'Arrosage',
                    'Fertilisation' => 'Fertilisation',
                    'Traitement phytosanitaire' => 'Traitement phytosanitaire',
                    'Récolte' => 'Récolte',
                    'Élagage / Taille' => 'Élagage / Taille',
                    'Greffage' => 'Greffage',
                ],
                'placeholder' => 'Sélectionnez un type',
                'attr' => ['class' => 'form-control'],
                'required' => true,
            ])
            ->add('culture', EntityType::class, [
                'class' => Culture::class,
                'choice_label' => 'nomCulture',
                'required' => false,
                'attr' => ['class' => 'form-control'],
                'placeholder' => 'Sélectionnez une culture',
                'query_builder' => function (EntityRepository $er) use ($user) {
                    return $er->createQueryBuilder('c')
                        ->join('c.parcelle', 'p')
                        ->where('p.utilisateur = :user')
                        ->setParameter('user', $user);
                },
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Activite::class,
            'user' => null, // Add the 'user' option
        ]);
    }
}