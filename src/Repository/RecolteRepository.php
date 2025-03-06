<?php

namespace App\Repository;

use App\Entity\Recolte;
use App\Entity\Utilisateurs;

use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Recolte>
 */
class RecolteRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Recolte::class);
    }
    public function findBySearchAndFilter(?string $searchTerm, ?string $qualiteFilter, string $sort, string $direction): array
    {
        $allowedSortFields = ['id', 'dateRecolte', 'quantite', 'qualite', 'culture.nomCulture', 'prixUnitaire']; 
        if (!in_array($sort, $allowedSortFields, true)) {
            $sort = 'dateRecolte'; 
        }
    
        $qb = $this->createQueryBuilder('r')
            ->leftJoin('r.culture', 'c')
            ->addSelect('c');
    
        if ($searchTerm) {
            $qb->andWhere('c.nomCulture LIKE :searchTerm')
               ->setParameter('searchTerm', '%' . $searchTerm . '%');
        }
    
        if ($qualiteFilter) {
            $qb->andWhere('r.qualite = :qualiteFilter')
               ->setParameter('qualiteFilter', $qualiteFilter);
        }
    
        $qb->orderBy('r.' . $sort, $direction);
    
        return $qb->getQuery()->getResult();
    }


    public function countByUser(Utilisateurs $user): int
{
    return $this->createQueryBuilder('r')
        ->select('COUNT(r.id)')
        ->leftJoin('r.culture', 'c')
        ->leftJoin('c.parcelle', 'p')
        ->where('p.utilisateur = :user')
        ->setParameter('user', $user)
        ->getQuery()
        ->getSingleScalarResult();
}



public function getTotalRevenueByUser(Utilisateurs $user): float
{
    return $this->createQueryBuilder('r')
        ->select('SUM(r.quantite * r.prixUnitaire) as total')
        ->leftJoin('r.culture', 'c')
        ->leftJoin('c.parcelle', 'p')
        ->where('p.utilisateur = :user')
        ->setParameter('user', $user)
        ->getQuery()
        ->getSingleScalarResult() ?? 0;
}

public function getRecolteMonthsByUser(Utilisateurs $user): array
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

    return $result->fetchAllAssociative();
}

public function getRecolteQuantitiesByUser(Utilisateurs $user): array
{
    $sql = "
        SELECT DATE_FORMAT(r.date_recolte, '%Y-%m') AS month, SUM(r.quantite) AS quantity
        FROM recolte r
        INNER JOIN culture c ON r.culture_id = c.id
        INNER JOIN parcelle p ON c.parcelle_id = p.id
        WHERE p.utilisateur_id = :user
        GROUP BY month
    ";

    $connection = $this->getEntityManager()->getConnection();
    $statement = $connection->prepare($sql);
    $result = $statement->executeQuery(['user' => $user->getId()]);

    return $result->fetchAllAssociative();
}

public function getRecolteQualityDistributionByUser(Utilisateurs $user): array
{
    return $this->createQueryBuilder('r')
        ->select('r.qualite as quality, COUNT(r.id) as count')
        ->leftJoin('r.culture', 'c')
        ->leftJoin('c.parcelle', 'p')
        ->where('p.utilisateur = :user')
        ->groupBy('r.qualite')
        ->setParameter('user', $user)
        ->getQuery()
        ->getResult();
}

public function getRecolteCultureTypesByUser(Utilisateurs $user): array
{
    $results = $this->createQueryBuilder('r')
        ->select('c.nomCulture as type')
        ->leftJoin('r.culture', 'c')
        ->leftJoin('c.parcelle', 'p')
        ->where('p.utilisateur = :user')
        ->groupBy('c.nomCulture')
        ->setParameter('user', $user)
        ->getQuery()
        ->getResult();

    // Extract the 'type' values into a simple array
    return array_column($results, 'type');
}

public function getRecolteCultureQuantitiesByUser(Utilisateurs $user): array
{
    $results = $this->createQueryBuilder('r')
        ->select('SUM(r.quantite) as quantity')
        ->leftJoin('r.culture', 'c')
        ->leftJoin('c.parcelle', 'p')
        ->where('p.utilisateur = :user')
        ->groupBy('c.nomCulture')
        ->setParameter('user', $user)
        ->getQuery()
        ->getResult();

    // Extract the 'quantity' values into a simple array
    return array_column($results, 'quantity');
}

}