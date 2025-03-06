<?php

namespace App\Repository;

use App\Entity\Activite;
use App\Entity\Utilisateurs;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Activite>
 */
class ActiviteRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Activite::class);
    }

    public function findByUser(Utilisateurs $user): array
{
    return $this->createQueryBuilder('a')
        ->join('a.culture', 'c')
        ->join('c.parcelle', 'p')
        ->where('p.utilisateur = :user')
        ->setParameter('user', $user)
        ->getQuery()
        ->getResult();
}


    public function findBySearchAndFilterBack(?string $searchTerm, ?string $typeFilter, string $sort, string $direction)
    {
        $qb = $this->createQueryBuilder('a')
            ->leftJoin('a.culture', 'c') 
            ->addSelect('c');

        if (!empty($searchTerm)) {
            $qb->andWhere('c.nomCulture LIKE :searchTerm')
               ->setParameter('searchTerm', '%' . $searchTerm . '%');
        }

        if (!empty($typeFilter)) {
            $qb->andWhere('a.type = :typeFilter')
               ->setParameter('typeFilter', $typeFilter);
        }

        if (in_array($sort, ['date', 'type'], true) && in_array(strtoupper($direction), ['ASC', 'DESC'], true)) {
            $qb->orderBy('a.' . $sort, $direction);
        } else {
            $qb->orderBy('a.date', 'ASC');
        }

        return $qb->getQuery()->getResult();
    }


    public function countByUser(Utilisateurs $user): int
{
    return $this->createQueryBuilder('a')
        ->select('COUNT(a.id)')
        ->leftJoin('a.culture', 'c')
        ->leftJoin('c.parcelle', 'p')
        ->where('p.utilisateur = :user')
        ->setParameter('user', $user)
        ->getQuery()
        ->getSingleScalarResult();
}


public function getActivityTypeDistributionByUser(Utilisateurs $user): array
{
    return $this->createQueryBuilder('a')
        ->select('a.type as type, COUNT(a.id) as count')
        ->leftJoin('a.culture', 'c')
        ->leftJoin('c.parcelle', 'p')
        ->where('p.utilisateur = :user')
        ->groupBy('a.type')
        ->setParameter('user', $user)
        ->getQuery()
        ->getResult();
}

public function getActivityCropDistributionByUser(Utilisateurs $user): array
{
    return $this->createQueryBuilder('a')
        ->select('c.nomCulture as crop, COUNT(a.id) as count')
        ->leftJoin('a.culture', 'c')
        ->leftJoin('c.parcelle', 'p')
        ->where('p.utilisateur = :user')
        ->groupBy('c.nomCulture')
        ->setParameter('user', $user)
        ->getQuery()
        ->getResult();
}


public function getActivityMonthsByUser(Utilisateurs $user): array
{
    $sql = "
        SELECT DATE_FORMAT(a.date, '%Y-%m') AS month
        FROM activite a
        INNER JOIN culture c ON a.culture_id = c.id
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

public function getActivityFrequenciesByUser(Utilisateurs $user): array
{
    $sql = "
        SELECT COUNT(a.id) AS frequency
        FROM activite a
        INNER JOIN culture c ON a.culture_id = c.id
        INNER JOIN parcelle p ON c.parcelle_id = p.id
        WHERE p.utilisateur_id = :user
        GROUP BY DATE_FORMAT(a.date, '%Y-%m')
    ";

    $connection = $this->getEntityManager()->getConnection();
    $statement = $connection->prepare($sql);
    $result = $statement->executeQuery(['user' => $user->getId()]);

    // Fetch the associative array
    $data = $result->fetchAllAssociative();

    // Extract the 'frequency' values into a simple array
    return array_column($data, 'frequency');
}

public function getActivityCulturesByUser(Utilisateurs $user): array
{
    return $this->createQueryBuilder('a')
        ->select('c.nomCulture as culture')
        ->leftJoin('a.culture', 'c')
        ->leftJoin('c.parcelle', 'p')
        ->where('p.utilisateur = :user')
        ->groupBy('c.nomCulture')
        ->setParameter('user', $user)
        ->getQuery()
        ->getResult();
}

public function getActivityCountsByUser(Utilisateurs $user): array
{
    $results = $this->createQueryBuilder('a')
        ->select('COUNT(a.id) as count')
        ->leftJoin('a.culture', 'c')
        ->leftJoin('c.parcelle', 'p')
        ->where('p.utilisateur = :user')
        ->groupBy('c.nomCulture')
        ->setParameter('user', $user)
        ->getQuery()
        ->getResult();

    // Extract the 'count' values into a simple array
    return array_column($results, 'count');
}

}
