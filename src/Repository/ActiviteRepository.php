<?php

namespace App\Repository;

use App\Entity\Activite;
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

    public function findBySearchAndFilter(?string $searchTerm, ?string $typeFilter, string $sort, string $direction)
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
}
