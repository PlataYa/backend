-- Insertar un usuario
INSERT INTO plataya_user (id, name, surname, mail, password)
VALUES (1, 'Tomás', 'Alcázar', 'tomas@mail.com', '1234');

-- Insertar una wallet asociada al usuario
INSERT INTO wallet (id, cvu, user_id, balance)
VALUES (1, 1234567890, 1, 1000.0);
