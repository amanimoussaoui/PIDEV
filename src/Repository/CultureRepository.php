<?php

namespace App\Repository;

use App\Entity\Culture;
use App\Entity\Utilisateurs;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Culture>
 */
class CultureRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Culture::class);
    }

    public function findBySearchAndFilter(string $searchTerm = '', string $statutFilter = '', string $sort = 'id', string $direction = 'ASC', ?Utilisateurs $user = null): array
    {
        $qb = $this->createQueryBuilder('c');
    
        if (!empty($searchTerm)) {
            $qb->andWhere('c.nom LIKE :searchTerm')
               ->setParameter('searchTerm', '%' . $searchTerm . '%');
        }
    
        if (!empty($statutFilter)) {
            $qb->andWhere('c.statut = :statutFilter')
               ->setParameter('statutFilter', $statutFilter);
        }
    
        if ($user) {
            $qb->join('c.parcelle', 'p')
               ->andWhere('p.utilisateur = :user')
               ->setParameter('user', $user);
        }
    
        // Add sorting
        $qb->orderBy('c.' . $sort, $direction);
    
        return $qb->getQuery()->getResult();
    }
 
    

    public function findBySearchAndFilterBack(?string $searchTerm, ?string $statutFilter, string $sort, string $direction): array
    {
        $allowedSortFields = ['id', 'nomCulture', 'statut', 'createdAt'];
        if (!in_array($sort, $allowedSortFields, true)) {
            $sort = 'id';
        }
    
        $qb = $this->createQueryBuilder('c');
    
        if ($searchTerm) {
            $qb->leftJoin('c.parcelle', 'p')
               ->andWhere('c.nomCulture LIKE :searchTerm OR p.nom LIKE :searchTerm')
               ->setParameter('searchTerm', '%' . $searchTerm . '%');
        }
    
        if ($statutFilter) {
            $qb->andWhere('c.statut = :statutFilter')
               ->setParameter('statutFilter', $statutFilter);
        }
    
        $qb->orderBy('c.' . $sort, $direction);
    
        return $qb->getQuery()->getResult();
    }
    

    public function countByUser(Utilisateurs $user): int
    {
        return $this->createQueryBuilder('c')
            ->select('COUNT(c.id)')
            ->leftJoin('c.parcelle', 'p')
            ->where('p.utilisateur = :user')
            ->setParameter('user', $user)
            ->getQuery()
            ->getSingleScalarResult();
    }
    
    public function getCropTypeDistributionByUser(Utilisateurs $user): array
    {
        return $this->createQueryBuilder('c')
            ->select('c.nomCulture as cropType, COUNT(c.id) as count')
            ->leftJoin('c.parcelle', 'p')
            ->where('p.utilisateur = :user')
            ->setParameter('user', $user)
            ->groupBy('c.nomCulture')
            ->getQuery()
            ->getResult();
    }
    
    public function getCropStatusDistributionByUser(Utilisateurs $user): array
    {
        return $this->createQueryBuilder('c')
            ->select('c.statut as status, COUNT(c.id) as count')
            ->leftJoin('c.parcelle', 'p')
            ->where('p.utilisateur = :user')
            ->setParameter('user', $user)
            ->groupBy('c.statut')
            ->getQuery()
            ->getResult();
    }


    public function getCultureTypesByUser(Utilisateurs $user): array
    {
        $results = $this->createQueryBuilder('c')
            ->select('c.nomCulture as type')
            ->leftJoin('c.parcelle', 'p')
            ->where('p.utilisateur = :user')
            ->groupBy('c.nomCulture')
            ->setParameter('user', $user)
            ->getQuery()
            ->getResult();
    
        // Extract the 'type' values into a simple array
        return array_column($results, 'type');
    }

    public function getCultureYieldsByUser(Utilisateurs $user): array
{
    $results = $this->createQueryBuilder('c')
        ->select('SUM(r.quantite) as yield')
        ->leftJoin('c.recoltes', 'r')
        ->leftJoin('c.parcelle', 'p')
        ->where('p.utilisateur = :user')
        ->groupBy('c.nomCulture')
        ->setParameter('user', $user)
        ->getQuery()
        ->getResult();

    // Extract the 'yield' values into a simple array
    return array_column($results, 'yield');
}

}
