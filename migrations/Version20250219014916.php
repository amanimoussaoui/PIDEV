<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20250219014916 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE candidature ADD utilisateur_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE candidature ADD CONSTRAINT FK_E33BD3B8FB88E14F FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id)');
        $this->addSql('CREATE INDEX IDX_E33BD3B8FB88E14F ON candidature (utilisateur_id)');
        $this->addSql('ALTER TABLE terrain DROP FOREIGN KEY FK_C87653B1FB88E14F');
        $this->addSql('ALTER TABLE terrain DROP FOREIGN KEY FK_C87653B1FB88E14F');
        $this->addSql('ALTER TABLE terrain ADD CONSTRAINT FK_C87653B1FB88E14F FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id)');
        $this->addSql('DROP INDEX fk_c87653b1fb88e14f ON terrain');
        $this->addSql('CREATE INDEX IDX_C87653B1FB88E14F ON terrain (utilisateur_id)');
        $this->addSql('ALTER TABLE terrain ADD CONSTRAINT FK_C87653B1FB88E14F FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id) ON DELETE CASCADE');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE candidature DROP FOREIGN KEY FK_E33BD3B8FB88E14F');
        $this->addSql('DROP INDEX IDX_E33BD3B8FB88E14F ON candidature');
        $this->addSql('ALTER TABLE candidature DROP utilisateur_id');
        $this->addSql('ALTER TABLE terrain DROP FOREIGN KEY FK_C87653B1FB88E14F');
        $this->addSql('ALTER TABLE terrain DROP FOREIGN KEY FK_C87653B1FB88E14F');
        $this->addSql('ALTER TABLE terrain ADD CONSTRAINT FK_C87653B1FB88E14F FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id) ON DELETE CASCADE');
        $this->addSql('DROP INDEX idx_c87653b1fb88e14f ON terrain');
        $this->addSql('CREATE INDEX FK_C87653B1FB88E14F ON terrain (utilisateur_id)');
        $this->addSql('ALTER TABLE terrain ADD CONSTRAINT FK_C87653B1FB88E14F FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id)');
    }
}
