create database if not exists CayoCartagena;
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
    Estado_oferta   enum('Pendiente', 'Publicada', 'Rechazada', 'Cancelada')
                        default 'Pendiente',
    Id_proveedor    int not null,

    foreign key (Id_proveedor) references Usuario(Id_usuario),
    check (Precio > 0),
    check (Cupos_maximos > 0)
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
--------------------------------------------------------------
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