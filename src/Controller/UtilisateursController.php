<?php

namespace App\Controller;

use App\Entity\Utilisateurs;
use App\Form\UpdateutilisateursType;
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
        $users = $a->findAll();

        return $this->render('utilisateurs/ListUtilisateurs.html.twig', [
            'tabusers' => $users,
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
}
