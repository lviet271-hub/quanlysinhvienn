CREATE DATABASE QuanLySinhVien;
GO
USE QuanLySinhVien;
GO

CREATE TABLE Lop (
    MaLop VARCHAR(20) PRIMARY KEY,
    TenLop NVARCHAR(100) NOT NULL
);

CREATE TABLE SinhVien (
    MaSV VARCHAR(20) PRIMARY KEY,
    HoTen NVARCHAR(100) NOT NULL,
    NgaySinh DATE NULL,
    GioiTinh NVARCHAR(10) NULL,
    DiaChi NVARCHAR(200) NULL,
    SoDienThoai VARCHAR(15) NULL,
    MaLop VARCHAR(20) NULL,
    CONSTRAINT FK_SinhVien_Lop FOREIGN KEY (MaLop) REFERENCES Lop(MaLop)
);

CREATE TABLE TaiKhoan (
    TenDangNhap VARCHAR(50) PRIMARY KEY,
    MatKhau VARCHAR(100) NOT NULL
);

INSERT INTO Lop(MaLop, TenLop) VALUES
('CNTT1', N'Công nghệ thông tin 1'),
('CNTT2', N'Công nghệ thông tin 2');

INSERT INTO SinhVien(MaSV, HoTen, NgaySinh, GioiTinh, DiaChi, SoDienThoai, MaLop)
VALUES
('SV001', N'Nguyễn Văn An', '2005-01-15', N'Nam', N'Hà Nội', '0912345678', 'CNTT1'),
('SV002', N'Trần Thị Bình', '2005-05-20', N'Nữ', N'Bắc Ninh', '0987654321', 'CNTT2');

-- Tài khoản demo: admin / 123456
INSERT INTO TaiKhoan(TenDangNhap, MatKhau) VALUES ('admin', '123456');
GO
