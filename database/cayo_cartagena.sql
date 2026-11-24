-- ------------------------------------------------------------
-- Cayo Cartagena - esquema completo (9 tablas)
-- Ejecutar una sola vez sobre una base vacia.
-- ------------------------------------------------------------
create database if not exists CayoCartagena
    character set utf8mb4 collate utf8mb4_unicode_ci;
use CayoCartagena;

-- ------------------------------------------------------------
-- Usuarios: cliente, proveedor, agencia y supervisor
-- ------------------------------------------------------------
create table Usuario (
    Id_usuario      int auto_increment primary key,
    Nombres         varchar(100) not null,
    Apellidos       varchar(100),
    Documento       varchar(20) not null unique,
    Correo          varchar(100) not null unique,
    Telefono        varchar(20),
    Contrasena      varchar(255) not null,
    Tipo_usuario    enum('Cliente', 'Proveedor', 'Agencia', 'Supervisor') not null
);

-- ------------------------------------------------------------
-- Datos del negocio del proveedor
-- ------------------------------------------------------------
create table Proveedor (
    Id_proveedor    int auto_increment primary key,
    Nombre_negocio  varchar(100) not null,
    Tipo_servicio   varchar(50) not null,
    Isla            varchar(100) not null,
    Descripcion     varchar(300),
    Id_usuario      int not null unique,

    foreign key (Id_usuario) references Usuario(Id_usuario)
);

-- ------------------------------------------------------------
-- Datos del perfil del turista (cliente)
-- ------------------------------------------------------------
create table Turista (
    Id_turista      int auto_increment primary key,
    Tipo_turista    enum('Turista', 'Residente') not null default 'Turista',
    Nacionalidad    varchar(60) not null,
    Id_usuario      int not null unique,

    foreign key (Id_usuario) references Usuario(Id_usuario)
);

-- ------------------------------------------------------------
-- Datos de la agencia de turismo
-- ------------------------------------------------------------
create table Agencia (
    Id_agencia          int auto_increment primary key,
    Razon_social        varchar(100) not null,
    Nit                 varchar(20) not null unique,
    Representante       varchar(100) not null,
    Cedula_representante varchar(20) not null,
    Rnt                 varchar(255) not null,
    Id_usuario          int not null unique,

    foreign key (Id_usuario) references Usuario(Id_usuario)
);

-- ------------------------------------------------------------
-- Ofertas publicadas por proveedores o agencias
-- ------------------------------------------------------------
create table Oferta (
    Id_oferta       int auto_increment primary key,
    Nombre_oferta   varchar(100) not null,
    Descripcion     varchar(500),
    Categoria       varchar(50) not null,
    Isla            varchar(100) not null,
    Precio          decimal(12,2) not null,
    Cupos_maximos   int not null,
    Dias_disponibles varchar(120),
    Horario_inicio  time,
    Horario_fin     time,
    Estado_oferta   enum('Pendiente', 'Publicada', 'Rechazada', 'Cancelada')
                        default 'Pendiente',
    Id_proveedor    int not null,

    foreign key (Id_proveedor) references Usuario(Id_usuario),
    check (Precio > 0),
    check (Cupos_maximos > 0)
);

-- ------------------------------------------------------------
-- Imágenes adjuntas a cada oferta
-- ------------------------------------------------------------
create table if not exists Oferta_imagen (
    Id_imagen   int auto_increment primary key,
    Id_oferta   int not null,
    Ruta        varchar(255) not null,

    foreign key (Id_oferta) references Oferta(Id_oferta) on delete cascade
);

-- ------------------------------------------------------------
-- Itinerarios creados por los clientes
-- ------------------------------------------------------------
create table Itinerario (
    Id_itinerario       int auto_increment primary key,
    Nombre_itinerario   varchar(100) not null,
    Destino             varchar(150) not null,
    Fecha_visita        date not null,
    Num_personas        int not null,
    Costo_total         decimal(12,2) not null default 0,
    Estado_itinerario   enum('Borrador', 'Pendiente de pago', 'Reservado', 'Cancelado')
                            default 'Borrador',
    Id_cliente          int not null,

    foreign key (Id_cliente) references Usuario(Id_usuario),
    check (Num_personas > 0),
    check (Costo_total >= 0)
);

-- ------------------------------------------------------------
-- Ofertas que forman parte de cada itinerario
-- ------------------------------------------------------------
create table Detalle_Itinerario (
    Id_detalle      int auto_increment primary key,
    Cantidad        int not null,
    Precio_unitario decimal(12,2) not null,
    Subtotal        decimal(12,2) not null,
    Id_itinerario   int not null,
    Id_oferta       int not null,

    foreign key (Id_itinerario) references Itinerario(Id_itinerario),
    foreign key (Id_oferta) references Oferta(Id_oferta),
    unique (Id_itinerario, Id_oferta),
    check (Cantidad > 0),
    check (Precio_unitario > 0),
    check (Subtotal >= 0)
);

-- ------------------------------------------------------------
-- Reserva y pago del itinerario
-- ------------------------------------------------------------
create table Reserva (
    Id_reserva          int auto_increment primary key,
    Fecha_reserva       datetime not null default current_timestamp,
    Valor_total         decimal(12,2) not null,
    Metodo_pago         varchar(30),
    Estado_pago         enum('Pendiente', 'Aprobado', 'Rechazado', 'Reembolsado')
                            not null default 'Pendiente',
    Estado_reserva      enum('Pendiente', 'Pagada', 'Cancelada')
                            not null default 'Pendiente',
    Valor_reembolso     decimal(12,2) not null default 0,
    Id_itinerario       int not null unique,

    foreign key (Id_itinerario) references Itinerario(Id_itinerario),
    check (Valor_total >= 0),
    check (Valor_reembolso >= 0)
);

-- ------------------------------------------------------------
-- Usuario supervisor para la revision de ofertas.
-- Contrasena: Supervisor123 (texto plano, prototipo academico).
-- ------------------------------------------------------------
insert into Usuario
    (Nombres, Apellidos, Documento, Correo, Telefono, Contrasena, Tipo_usuario)
values
    ('Supervisor', 'Operativo', '900001', 'supervisor@cayocartagena.com', '3000000001',
     'Supervisor123', 'Supervisor');
