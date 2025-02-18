<?php

namespace App\Controller;

use App\Entity\Machine;
use App\Repository\MachineRepository;
use App\Form\MachineType;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class GestionMachineController extends AbstractController
{
    #[Route('/gestion/machines', name: 'gestion_machine_index')]
    public function index(): Response
    {
        return $this->render('gestion_machine/index.html.twig');
    }

    #[Route('/gestion/showmachines', name: 'gestion_machine_show')]
    public function showMachines(MachineRepository $repo): Response
    {
        return $this->render('gestion_machine/show_machines.html.twig', [
            'tabmachines' => $repo->findAll(),
        ]);
    }

    #[Route('/gestion/addformmachine', name: 'gestion_machine_addform')]
    public function addFormMachine(ManagerRegistry $doctrine, Request $request): Response
    {
        $machine = new Machine();
        $form = $this->createForm(MachineType::class, $machine);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em = $doctrine->getManager();
            $em->persist($machine);
            $em->flush();
            return $this->redirectToRoute('gestion_machine_show');
        }

        return $this->render('gestion_machine/addformmachine.html.twig', [
            'formadd' => $form->createView(),
        ]);
    }

    #[Route('/gestion/updatemachine/{id}', name: 'gestion_machine_update')]
    public function updateMachine(ManagerRegistry $doctrine, Request $request, Machine $machine): Response
    {
        $form = $this->createForm(MachineType::class, $machine);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $doctrine->getManager()->flush();
            return $this->redirectToRoute('gestion_machine_show');
        }

        return $this->render('gestion_machine/addformmachine.html.twig', [
            'formadd' => $form->createView(),
        ]);
    }

    #[Route('/gestion/deletemachine/{id}', name: 'gestion_machine_delete')]
    public function deleteMachine(ManagerRegistry $doctrine, Machine $machine): Response
    {
        $em = $doctrine->getManager();
        $em->remove($machine);
        $em->flush();
        return $this->redirectToRoute('gestion_machine_show');
    }

    #[Route('/gestion/showmachinesfront', name: 'gestion_machine_show_front')]
    public function showMachinesfront(MachineRepository $repo): Response
    {
        return $this->render('gestion_machine/show_machine_front.html.twig', [
            'tabmachines' => $repo->findAll(),
        ]);
    }

    #[Route('/gestion/showmachinesfront2', name: 'gestion_machine_show_front2')]
    public function showMachinesfront2(MachineRepository $repo): Response
    {
        return $this->render('gestion_machine/show_machine_front2.html.twig', [
            'tabmachines' => $repo->findBy(['disponibilite' => 'oui']),
        ]);
    }

    #[Route('/gestion/addformmachinefront', name: 'gestion_machine_addformfront')]
    public function addFormMachinefront(ManagerRegistry $doctrine, Request $request): Response
    {
        $machine = new Machine();
        $form = $this->createForm(MachineType::class, $machine);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $doctrine->getManager()->persist($machine);
            $doctrine->getManager()->flush();
            return $this->redirectToRoute('gestion_machine_show_front');
        }

        return $this->render('gestion_machine/addformmachinefront.html.twig', [
            'formadd' => $form->createView(),
        ]);
    }

    #[Route('/gestion/updatemachinefront/{id}', name: 'gestion_machine_updatefront')]
    public function updateMachinefront(ManagerRegistry $doctrine, Request $request, Machine $machine): Response
    {
        $form = $this->createForm(MachineType::class, $machine);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $doctrine->getManager()->flush();
            return $this->redirectToRoute('gestion_machine_show_front');
        }

        return $this->render('gestion_machine/addformmachinefront.html.twig', [
            'formadd' => $form->createView(),
        ]);
    }

    #[Route('/gestion/deletemachinefront/{id}', name: 'gestion_machine_deletefront')]
    public function deleteMachinefront(ManagerRegistry $doctrine, Machine $machine): Response
    {
        $em = $doctrine->getManager();
        $em->remove($machine);
        $em->flush();
        return $this->redirectToRoute('gestion_machine_show_front');
    }

    #[Route('/gestion/materiel', name: 'gestion_materiel')]
    public function gestionMateriel(): Response
    {
        return $this->render('gestion_machine/initiale_front.html.twig');
    }

    #[Route('/gestion/machine/{id}', name: 'gestion_machine_details')]
    public function show(Machine $machine): Response
    {
        return $this->render('machine/show.html.twig', [
            'machine' => $machine,
        ]);
    }
}
