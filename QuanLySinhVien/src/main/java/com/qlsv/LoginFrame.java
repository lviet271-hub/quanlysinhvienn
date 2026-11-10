package com.qlsv;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginFrame extends JFrame {
    private final JTextField txtUser = new JTextField(18);
    private final JPasswordField txtPass = new JPasswordField(18);

    public LoginFrame() {
        setTitle("Đăng nhập - Quản lý sinh viên");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(380, 230);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 12));
        form.setBorder(BorderFactory.createEmptyBorder(25, 25, 15, 25));
        form.add(new JLabel("Tên đăng nhập:"));
        form.add(txtUser);
        form.add(new JLabel("Mật khẩu:"));
        form.add(txtPass);
        JButton btnLogin = new JButton("Đăng nhập");
        JButton btnExit = new JButton("Thoát");
        form.add(btnLogin);
        form.add(btnExit);
        add(form);

        btnLogin.addActionListener(e -> login());
        btnExit.addActionListener(e -> System.exit(0));
        getRootPane().setDefaultButton(btnLogin);
    }

    private void login() {
        String sql = "SELECT TenDangNhap FROM TaiKhoan WHERE TenDangNhap=? AND MatKhau=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtUser.getText().trim());
            ps.setString(2, new String(txtPass.getPassword()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
                    dispose();
                    new StudentFrame().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Sai tên đăng nhập hoặc mật khẩu.");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Không kết nối được cơ sở dữ liệu. Kiểm tra SQL Server và DBConnection.java.\n" + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
