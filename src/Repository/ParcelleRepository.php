<?php

namespace App\Repository;

use App\Entity\Parcelle;
use App\Entity\Utilisateurs;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Parcelle>
 */
class ParcelleRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Parcelle::class);
    }

  

    public function findBySearchCriteria(array $criteria, string $sort = 'superficie', string $direction = 'ASC', ?Utilisateurs $user = null): array
    {
        $qb = $this->createQueryBuilder('p');
    
        // Add search criteria
        if (!empty($criteria['nom'])) {
            $qb->andWhere('p.nom LIKE :nom')
               ->setParameter('nom', '%' . $criteria['nom'] . '%');
        }
    
        if (!empty($criteria['localisation'])) {
            $qb->andWhere('p.localisation LIKE :localisation')
               ->setParameter('localisation', '%' . $criteria['localisation'] . '%');
        }
    
        if (!empty($criteria['typeSol'])) {
            $qb->andWhere('p.typeSol = :typeSol')
               ->setParameter('typeSol', $criteria['typeSol']);
        }
    
        // Filter by the logged-in user
        if ($user) {
            $qb->andWhere('p.utilisateur = :user')
               ->setParameter('user', $user);
        }
    
        // Add sorting
        $qb->orderBy('p.' . $sort, $direction);
    
        return $qb->getQuery()->getResult();
    }


    public function findBySearchCriteriaQuery(array $criteria, string $sort = 'superficie', string $direction = 'ASC')
    {
        $queryBuilder = $this->createQueryBuilder('p');
    
        // Filtrer par nom si un nom est fourni
        if (!empty($criteria['nom'])) {
            $queryBuilder->andWhere('p.nom LIKE :nom')
                ->setParameter('nom', '%' . $criteria['nom'] . '%');
        }
    
        // Filtrer par type de sol si un type est sélectionné
        if (!empty($criteria['typeSol'])) {
            $queryBuilder->andWhere('p.typeSol = :typeSol')
                ->setParameter('typeSol', $criteria['typeSol']);
        }
    
        // Trier par le champ spécifié (par défaut : superficie)
        $queryBuilder->orderBy('p.' . $sort, $direction);
    
        return $queryBuilder->getQuery()->getResult();
    }

    public function getSoilTypeDistributionByUser(Utilisateurs $user): array
    {
        return $this->createQueryBuilder('p')
            ->select('p.typeSol as soilType, COUNT(p.id) as count')
            ->where('p.utilisateur = :user')
            ->setParameter('user', $user)
            ->groupBy('p.typeSol')
            ->getQuery()
            ->getResult();
    }
    


    public function getParcelleYieldMonthsByUser(Utilisateurs $user): array
{
    $sql = "
        SELECT DATE_FORMAT(r.date_recolte, '%Y-%m') AS month
        FROM recolte r
        INNER JOIN culture c ON r.culture_id = c.id
        INNER JOIN parcelle p ON c.parcelle_id = p.id
        WHERE p.utilisateur_id = :user
        GROUP BY month
    ";

    $connection = $this->getEntityManager()->getConnection();
    $statement = $connection->prepare($sql);
    $result = $statement->executeQuery(['user' => $user->getId()]);

    // Fetch the associative array
    $data = $result->fetchAllAssociative();

    // Extract the 'month' values into a simple array
    return array_column($data, 'month');
}
    
    public function getParcelleYieldDataByUser(Utilisateurs $user): array
    {
        $sql = "
            SELECT p.nom AS nom, DATE_FORMAT(r.date_recolte, '%Y-%m') AS month, SUM(r.quantite) AS yield
            FROM recolte r
            INNER JOIN culture c ON r.culture_id = c.id
            INNER JOIN parcelle p ON c.parcelle_id = p.id
            WHERE p.utilisateur_id = :user
            GROUP BY p.id, month
        ";
    
        $connection = $this->getEntityManager()->getConnection();
        $statement = $connection->prepare($sql);
        $result = $statement->executeQuery(['user' => $user->getId()]);
    
        // Fetch the associative array
        $data = $result->fetchAllAssociative();
    
        // Organize data by parcel
        $parcelleYieldData = [];
        foreach ($data as $row) {
            $nom = $row['nom'];
            $month = $row['month'];
            $yield = (float)$row['yield'];
    
            if (!isset($parcelleYieldData[$nom])) {
                $parcelleYieldData[$nom] = [
                    'nom' => $nom,
                    'yields' => [],
                ];
            }
    
            $parcelleYieldData[$nom]['yields'][$month] = $yield;
        }
    
        // Ensure yields are in the correct order (matching parcelleYieldMonths)
        $parcelleYieldMonths = $this->getParcelleYieldMonthsByUser($user);
        foreach ($parcelleYieldData as &$parcelle) {
            $parcelle['yields'] = array_map(function ($month) use ($parcelle) {
                return $parcelle['yields'][$month] ?? 0; // Fill missing months with 0
            }, $parcelleYieldMonths);
        }
    
        return array_values($parcelleYieldData);
    }

}
