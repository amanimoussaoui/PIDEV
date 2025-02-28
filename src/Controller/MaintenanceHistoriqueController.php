<?php

namespace App\Controller;

use App\Entity\Machine;
use Doctrine\ORM\EntityManagerInterface;
use App\Entity\Maintenance;




use App\Repository\MachineRepository;
use App\Repository\MaintenanceHistoriqueRepository;
use App\Entity\MaintenanceHistorique;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
class MaintenanceHistoriqueController extends AbstractController
{
    #[Route('/verifier-maintenance', name: 'verifier_maintenance')]
public function verifierMaintenance(EntityManagerInterface $entityManager): Response
{
    $dateActuelle = new \DateTimeImmutable();  // Créez une instance de DateTimeImmutable

    // Récupérer les machines dont la date de maintenance est dans le passé
    $machines = $entityManager->getRepository(Machine::class)->createQueryBuilder('m')
        ->where('m.date_maintenance <= :dateActuelle')
        ->setParameter('dateActuelle', $dateActuelle)  // Utilisez DateTimeImmutable ici
        ->getQuery()
        ->getResult();

    foreach ($machines as $machine) {
        // Vérifier si la machine a une maintenance associée dans l'historique
        $historiqueExistant = $entityManager->getRepository(MaintenanceHistorique::class)->findOneBy([
            'machine' => $machine,
            'dateMaintenance' => $machine->getDateMaintenance()  // Comparer avec la date de maintenance de la machine
        ]);

        if (!$historiqueExistant) {
            // Ajouter la maintenance à l'historique
            $historique = new MaintenanceHistorique();
            $historique->setMachine($machine);
            $historique->setDateMaintenance($machine->getDateMaintenance());  // Assurez-vous que dateMaintenance est de type DateTimeImmutable

            $entityManager->persist($historique);
        }
    }

    // Exécuter la sauvegarde après la boucle pour optimiser la performance
    $entityManager->flush();

    return $this->redirectToRoute('maintenance_historique_index');
}

    


    #[Route('/maintenance-historique', name: 'maintenance_historique_index')]
    public function index(MaintenanceHistoriqueRepository $maintenanceHistoriqueRepo): Response
    {
        // Récupérer toutes les maintenances historiques depuis la base de données
        $maintenances = $maintenanceHistoriqueRepo->findAll();

        // Afficher dans le template
        return $this->render('maintenance_historique/index.html.twig', [
            'maintenances' => $maintenances,
        ]);
    }
}
