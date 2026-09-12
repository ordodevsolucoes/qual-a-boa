-- hashes gerados com BCryptPasswordEncoder(10); senha em claro documentada na entrega, nunca versionada em outro lugar
INSERT INTO usuario (email, senha_hash, nome, papel) VALUES
    ('participante@qualaboa.dev', '$2a$10$wzTBtY3XXelA8bCrN9pCL.6s5zvoeWXELSujk.hwtwFYmYxp/d6.e', 'Participante Semente', 'PARTICIPANTE'),
    ('local@qualaboa.dev', '$2a$10$XAHKeukAbV6mt5cSaznS2OigfmE0kjznbyCvCWzYHRAQATD6RzSWq', 'Local de Curso Semente', 'LOCAL_DE_CURSO');
