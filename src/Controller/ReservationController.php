<?php

namespace App\Controller;

use App\Entity\Reservation;
use App\Entity\Machine;
use App\Form\ReservationType;
use App\Repository\ReservationRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Doctrine\Persistence\ManagerRegistry;

#[Route('/reservation')]
class ReservationController extends AbstractController
{
    #[Route('/reservations', name: 'reservation_index')]
    public function index(ReservationRepository $reservationRepository): Response
    {
        $reservations = $reservationRepository->findAll(); // Récupère toutes les réservations
        return $this->render('reservation/index.html.twig', [
            'tabreservations' => $reservations, // Passe les réservations à la vue
        ]);
    }
    #[Route('/reservations2', name: 'reservation_index2')]
    public function index2(ReservationRepository $reservationRepository): Response
    {
        $reservations = $reservationRepository->findAll(); // Récupère toutes les réservations
        return $this->render('reservation/index2.html.twig', [
            'tabreservations' => $reservations, // Passe les réservations à la vue
        ]);
    }
    



    #[Route('/new/{id_machine}', name: 'reservation_new')]
    public function new(Request $request, EntityManagerInterface $entityManager, Machine $id_machine): Response
    {
        $reservation = new Reservation();
        $reservation->setIdMachine($id_machine); // Associer la machine

        $form = $this->createForm(ReservationType::class, $reservation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($reservation);
            $entityManager->flush();

            return $this->redirectToRoute('reservation_index');
        }

        return $this->render('reservation/new.html.twig', [
            'reservation' => $reservation,
            'form' => $form->createView(),
        ]);
    }

    

    #[Route('/{id}/edit', name: 'reservation_edit',)]
    public function edit(Request $request, Reservation $reservation, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(ReservationType::class, $reservation);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            return $this->redirectToRoute('reservation_index');
        }

        return $this->render('reservation/edit.html.twig', [
            'reservation' => $reservation,
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}', name: 'reservation_delete')]
    public function delete(ManagerRegistry $doctrine, $id, ReservationRepository $repo): Response
    {
        $em = $doctrine->getManager();
        $reservation = $repo->find($id); // Récupère la réservation par son ID
    
        if ($reservation) {
            $em->remove($reservation); // Supprime la réservation
            $em->flush(); // Enregistre la suppression
        }
    
        return $this->redirectToRoute('reservation_index'); // Redirige vers la liste des réservations
    }
}
