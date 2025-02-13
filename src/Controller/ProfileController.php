<?php

namespace App\Controller;

use App\Entity\Profile;
use App\Entity\Utilisateurs;
use App\Form\ProfileType;
use Symfony\Component\Security\Http\Attribute\IsGranted;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\HttpFoundation\Request;
use App\Repository\ProfileRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Core\User\UserInterface;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Component\DependencyInjection\Attribute\Autowire;

final class ProfileController extends AbstractController
{
    #[Route('/profile', name: 'app_profile')]
    public function index(): Response
    {
        return $this->render('profile/index.html.twig', [
            'controller_name' => 'ProfileController',
        ]);
    }

    #[Route('/profile', name: 'app_profile')]
    #[IsGranted('IS_AUTHENTICATED_FULLY')] // Assure que l'utilisateur est connecté
    public function showprofile(ProfileRepository $profileRepository): Response
    {
        // Récupérer l'utilisateur connecté
        $user = $this->getUser();

        // Récupérer le profil lié à l'utilisateur
        $profile = $profileRepository->findOneBy(['id_user' => $user]);

        // Vérifier si le profil existe
        if (!$profile) {
            $this->addFlash('error', 'Aucun profil trouvé pour cet utilisateur.');
            return $this->redirectToRoute('app_home'); // Rediriger si aucun profil
        }

        return $this->render('profile/profile.html.twig', [
            'profile' => $profile,
        ]);
    }

    #[Route('/updateprofile/{id}', name: 'app_updateprofile')]
    public function updateauthor(ManagerRegistry $m , Request $req , $id ,ProfileRepository $rep,#[Autowire('%photo_dir%')]string $photoDir): Response
    {
        $em=$m->getManager();
        $profile=$rep->find($id);
        $form=$this->createForm(ProfileType::class,$profile);
        $form->handleRequest($req);
        if($form->isSubmitted() && $form->isValid())
        {
            if($photo = $form['image']->getData()){
                $filename=uniqid().'.'.$photo->guessExtension();
                $photo->move($photoDir,$filename);
                $profile->setImage($filename); // Mettre à jour le champ image de l'entité Profile
            }
            $em->persist($profile);
            $em->flush();
            return $this->redirectToRoute('app_profile');
        }
        
        return $this->render('profile/updateprofile.html.twig', [
            'formupdateprofile' => $form,
        ]);
    }
}
