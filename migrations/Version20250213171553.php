<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20250213171553 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE maintenance_historique ADD maintenance_id INT NOT NULL');
        $this->addSql('ALTER TABLE maintenance_historique ADD CONSTRAINT FK_CAF85562F6C202BC FOREIGN KEY (maintenance_id) REFERENCES maintenance_historique (id)');
        $this->addSql('CREATE INDEX IDX_CAF85562F6C202BC ON maintenance_historique (maintenance_id)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE maintenance_historique DROP FOREIGN KEY FK_CAF85562F6C202BC');
        $this->addSql('DROP INDEX IDX_CAF85562F6C202BC ON maintenance_historique');
        $this->addSql('ALTER TABLE maintenance_historique DROP maintenance_id');
    }
}
