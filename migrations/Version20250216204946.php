<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20250216204946 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE maintenance_historique DROP type_maintenance');
        $this->addSql('ALTER TABLE reservation DROP FOREIGN KEY FK_42C84955533DDBF1');
        $this->addSql('ALTER TABLE reservation ADD CONSTRAINT FK_42C84955533DDBF1 FOREIGN KEY (id_machine_id) REFERENCES machine (id)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE maintenance_historique ADD type_maintenance VARCHAR(255) NOT NULL');
        $this->addSql('ALTER TABLE reservation DROP FOREIGN KEY FK_42C84955533DDBF1');
        $this->addSql('ALTER TABLE reservation ADD CONSTRAINT FK_42C84955533DDBF1 FOREIGN KEY (id_machine_id) REFERENCES machine (id) ON DELETE CASCADE');
    }
}
