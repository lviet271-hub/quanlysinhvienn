package com.qlsv;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class StudentFrame extends JFrame {
    private final JTextField txtMa = new JTextField(12);
    private final JTextField txtTen = new JTextField(18);
    private final JTextField txtNgaySinh = new JTextField(12); // yyyy-MM-dd
    private final JComboBox<String> cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
    private final JTextField txtDiaChi = new JTextField(18);
    private final JTextField txtSDT = new JTextField(12);
    private final JComboBox<String> cbLop = new JComboBox<>();
    private final JTextField txtTim = new JTextField(15);
    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"Mã SV", "Họ tên", "Ngày sinh", "Giới tính", "Địa chỉ", "SĐT", "Mã lớp"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public StudentFrame() {
        setTitle("PHẦN MỀM QUẢN LÝ SINH VIÊN");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 620);
        setLocationRelativeTo(null);
        buildUI();
        loadClasses();
        loadStudents("");
    }

    private void buildUI() {
        JPanel input = new JPanel(new GridLayout(4, 4, 8, 8));
        input.setBorder(BorderFactory.createTitledBorder("Thông tin sinh viên"));
        input.add(new JLabel("Mã sinh viên:")); input.add(txtMa);
        input.add(new JLabel("Họ tên:")); input.add(txtTen);
        input.add(new JLabel("Ngày sinh (yyyy-MM-dd):")); input.add(txtNgaySinh);
        input.add(new JLabel("Giới tính:")); input.add(cbGioiTinh);
        input.add(new JLabel("Địa chỉ:")); input.add(txtDiaChi);
        input.add(new JLabel("Số điện thoại:")); input.add(txtSDT);
        input.add(new JLabel("Lớp:")); input.add(cbLop);
        input.add(new JLabel("Tìm mã/tên:")); input.add(txtTim);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        JButton add = new JButton("Thêm");
        JButton update = new JButton("Sửa");
        JButton delete = new JButton("Xóa");
        JButton search = new JButton("Tìm kiếm");
        JButton refresh = new JButton("Làm mới");
        JButton classes = new JButton("Quản lý lớp");
        buttons.add(add); buttons.add(update); buttons.add(delete);
        buttons.add(search); buttons.add(refresh); buttons.add(classes);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        top.add(input, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add.addActionListener(e -> addStudent());
        update.addActionListener(e -> updateStudent());
        delete.addActionListener(e -> deleteStudent());
        search.addActionListener(e -> loadStudents(txtTim.getText().trim()));
        refresh.addActionListener(e -> { clearForm(); txtTim.setText(""); loadStudents(""); });
        classes.addActionListener(e -> new ClassFrame().setVisible(true));
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) fillForm();
        });
    }

    private void loadClasses() {
        cbLop.removeAllItems();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT MaLop FROM Lop ORDER BY MaLop");
             ResultSet r = p.executeQuery()) {
            while (r.next()) cbLop.addItem(r.getString("MaLop"));
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loadStudents(String keyword) {
        model.setRowCount(0);
        String sql = "SELECT MaSV, HoTen, NgaySinh, GioiTinh, DiaChi, SoDienThoai, MaLop " +
                     "FROM SinhVien WHERE MaSV LIKE ? OR HoTen LIKE ? ORDER BY MaSV";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, "%" + keyword + "%");
            p.setString(2, "%" + keyword + "%");
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    model.addRow(new Object[]{r.getString("MaSV"), r.getString("HoTen"),
                        r.getDate("NgaySinh"), r.getString("GioiTinh"), r.getString("DiaChi"),
                        r.getString("SoDienThoai"), r.getString("MaLop")});
                }
            }
        } catch (Exception ex) { showError(ex); }
    }

    private boolean validateForm() {
        if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã sinh viên và họ tên không được để trống.");
            return false;
        }
        if (!txtNgaySinh.getText().trim().isEmpty()) {
            try { LocalDate.parse(txtNgaySinh.getText().trim()); }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ngày sinh phải có dạng yyyy-MM-dd, ví dụ 2005-08-20.");
                return false;
            }
        }
        return true;
    }

    private void addStudent() {
        if (!validateForm()) return;
        String sql = "INSERT INTO SinhVien(MaSV,HoTen,NgaySinh,GioiTinh,DiaChi,SoDienThoai,MaLop) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            setStudentParameters(p);
            p.executeUpdate();
            JOptionPane.showMessageDialog(this, "Thêm sinh viên thành công.");
            loadStudents(""); clearForm();
        } catch (Exception ex) { showError(ex); }
    }

    private void updateStudent() {
        if (!validateForm()) return;
        String sql = "UPDATE SinhVien SET HoTen=?,NgaySinh=?,GioiTinh=?,DiaChi=?,SoDienThoai=?,MaLop=? WHERE MaSV=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, txtTen.getText().trim());
            setNullableDate(p, 2);
            p.setString(3, (String) cbGioiTinh.getSelectedItem());
            p.setString(4, txtDiaChi.getText().trim());
            p.setString(5, txtSDT.getText().trim());
            p.setString(6, (String) cbLop.getSelectedItem());
            p.setString(7, txtMa.getText().trim());
            if (p.executeUpdate() > 0) JOptionPane.showMessageDialog(this, "Cập nhật thành công.");
            else JOptionPane.showMessageDialog(this, "Không tìm thấy mã sinh viên.");
            loadStudents("");
        } catch (Exception ex) { showError(ex); }
    }

    private void deleteStudent() {
        String ma = txtMa.getText().trim();
        if (ma.isEmpty()) { JOptionPane.showMessageDialog(this, "Hãy chọn sinh viên cần xóa."); return; }
        if (JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa " + ma + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM SinhVien WHERE MaSV=?")) {
            p.setString(1, ma);
            p.executeUpdate();
            loadStudents(""); clearForm();
        } catch (Exception ex) { showError(ex); }
    }

    private void setStudentParameters(PreparedStatement p) throws SQLException {
        p.setString(1, txtMa.getText().trim());
        p.setString(2, txtTen.getText().trim());
        setNullableDate(p, 3);
        p.setString(4, (String) cbGioiTinh.getSelectedItem());
        p.setString(5, txtDiaChi.getText().trim());
        p.setString(6, txtSDT.getText().trim());
        p.setString(7, (String) cbLop.getSelectedItem());
    }

    private void setNullableDate(PreparedStatement p, int index) throws SQLException {
        String value = txtNgaySinh.getText().trim();
        if (value.isEmpty()) p.setNull(index, Types.DATE);
        else p.setDate(index, Date.valueOf(value));
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        txtMa.setText(String.valueOf(model.getValueAt(row, 0)));
        txtTen.setText(String.valueOf(model.getValueAt(row, 1)));
        Object date = model.getValueAt(row, 2);
        txtNgaySinh.setText(date == null ? "" : date.toString());
        cbGioiTinh.setSelectedItem(model.getValueAt(row, 3));
        txtDiaChi.setText(String.valueOf(model.getValueAt(row, 4)));
        txtSDT.setText(String.valueOf(model.getValueAt(row, 5)));
        cbLop.setSelectedItem(model.getValueAt(row, 6));
    }

    private void clearForm() {
        txtMa.setText(""); txtTen.setText(""); txtNgaySinh.setText("");
        txtDiaChi.setText(""); txtSDT.setText("");
        if (cbGioiTinh.getItemCount() > 0) cbGioiTinh.setSelectedIndex(0);
        table.clearSelection(); txtMa.requestFocus();
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
