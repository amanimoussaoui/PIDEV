<?php
namespace App\Form;

use App\Entity\Parcelle;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\OptionsResolver\OptionsResolver;

class ParcelleType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nom')
            ->add('superficie')
            ->add('localisation')
            ->add('typeSol', ChoiceType::class, [
                'choices' => Parcelle::getTypeSolChoices(),
                'placeholder' => 'Sélectionnez un type',
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Parcelle::class,
        ]);
    }
}
