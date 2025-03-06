<?php
namespace App\Form;

use App\Entity\Parcelle;
use App\Entity\Terrain;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use App\Repository\TerrainRepository;

class ParcelleType extends AbstractType
{

    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $user = $options['user']; // Get the user from the options

        $builder
            ->add('nom')
            ->add('superficie')
            ->add('localisation')
            ->add('typeSol', ChoiceType::class, [
                'choices' => Parcelle::getTypeSolChoices(),
                'placeholder' => 'Sélectionnez un type',
            ])
            ->add('terrain', EntityType::class, [
                'class' => Terrain::class, // The entity to use
                'choice_label' => 'description', // Display the "localisation" field of Terrain
                'placeholder' => 'Sélectionnez un terrain', // Add a placeholder
                'required' => true, // Make the field required
                'attr' => [
                    'class' => 'form-control', // Add Bootstrap class
                ],
                'query_builder' => function (TerrainRepository $terrainRepository) use ($user) {
                    // Filter terrains by the logged-in user
                    return $terrainRepository->createQueryBuilder('t')
                        ->where('t.utilisateur = :user')
                        ->setParameter('user', $user);
                },
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Parcelle::class,
            'user' => null, // Add a default value for the user option
        ]);
    }
}