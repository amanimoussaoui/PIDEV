<?php

namespace App\Repository;

use App\Entity\Culture;
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

    public function findBySearchAndFilter(?string $searchTerm, ?string $statutFilter, string $sort, string $direction): array
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
    
}
