<?php

namespace App\Controller;

use App\Entity\Utilisateurs;
use App\Form\UpdateutilisateursType;
use App\Form\RechercheUtilisateurType;
use App\Repository\UtilisateursRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

use Doctrine\Persistence\ManagerRegistry;

final class UtilisateursController extends AbstractController
{
    #[Route('/utilisateurs', name: 'app_utilisateurs')]
    public function index(): Response
    {
        return $this->render('utilisateurs/index.html.twig', [
            'controller_name' => 'UtilisateursController',
        ]);
    }
    //////////////////////////////////////////////////////////////////////
    #[Route('/showutilisateurs', name: 'app_showutilisateurs')]
    public function showutilisateurs(UtilisateursRepository $a , Request $req): Response
    {
        $form = $this->createForm(RechercheUtilisateurType::class);
        $form->handleRequest($req);

        $users = $a->findAll(); // Par défaut, afficher tous les utilisateurs
        $isSearch = false;

        if ($form->isSubmitted() && $form->isValid()) {
            $searchValue = $form->get('search')->getData();
            
            if ($searchValue) {
                $users = $a->listutilisateursbyid($searchValue);
                $isSearch = true;
            }
        }

        return $this->render('utilisateurs/ListUtilisateurs.html.twig', [
            'tabusers' => $users,
            'form' => $form->createView(),
            'isSearch' => $isSearch,
        ]);
    }
    ///////////////////////////////////////////////////////////////////////
    #[Route('/deleteautilisateurs/{id}', name: 'app_deleteutilisateurs')]
    public function deleteuser(ManagerRegistry $m , Request $req , $id ,UtilisateursRepository $rep): Response
    {
        $em=$m->getManager();
        $users=$rep->find($id);
        
        $em->remove($users);
        $em->flush();
        return $this->redirectToRoute('app_showutilisateurs');
    }
    ///////////////////////////////////////////////////////////////////////
    #[Route('/updateutilisateurs/{id}', name: 'app_updateutilisateurs')]
    public function updateauthor(ManagerRegistry $m , Request $req , $id ,UtilisateursRepository $rep): Response
    {
        $em=$m->getManager();
        $user=$rep->find($id);
        $form=$this->createForm(UpdateutilisateursType::class,$user);
        $form->handleRequest($req);
        if($form->isSubmitted() && $form->isValid())
        {
            $em->persist($user);
            $em->flush();
            return $this->redirectToRoute('app_showutilisateurs');
        }
        
        return $this->render('utilisateurs/updateuser.html.twig', [
            'formupdate' => $form,
        ]);
    }
    ////////////////////////////////////////////////////////////////////////
    #[Route('/chatbot', name: 'app_chatbot')]
    public function chatbot(): Response
    {
        return $this->render('Deepseek/chatbot.html.twig');
    }
    ///////////////////////////////////////////////////////////////////
    #[Route('/roles-pie-chart', name: 'app_roles_pie_chart')]
    public function rolesPieChart(UtilisateursRepository $repo): Response
    {
        // Récupérer les utilisateurs
        $users = $repo->findAll();

        // Calculer le nombre de chaque rôle
        $roleCounts = [];
        foreach ($users as $user) {
            foreach ($user->getRoles() as $role) {
                $role = str_replace('ROLE_', '', $role); // Nettoyer les rôles pour un affichage lisible
                if (!isset($roleCounts[$role])) {
                    $roleCounts[$role] = 0;
                }
                $roleCounts[$role]++;
            }
        }

        // Passer les données à la vue
        return $this->render('utilisateurs/roles_pie_chart.html.twig', [
            'roleCounts' => $roleCounts,
        ]);
    }
}
