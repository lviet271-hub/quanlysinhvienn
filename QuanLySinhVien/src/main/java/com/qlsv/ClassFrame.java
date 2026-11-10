package com.qlsv;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ClassFrame extends JFrame {
    private final JTextField txtMa = new JTextField(12);
    private final JTextField txtTen = new JTextField(20);
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Mã lớp", "Tên lớp"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public ClassFrame() {
        setTitle("Quản lý lớp học");
        setSize(550, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 5, 12));
        form.add(new JLabel("Mã lớp:")); form.add(txtMa);
        form.add(new JLabel("Tên lớp:")); form.add(txtTen);
        JButton add = new JButton("Thêm");
        JButton update = new JButton("Sửa");
        JButton delete = new JButton("Xóa");
        JButton refresh = new JButton("Làm mới");
        JPanel buttons = new JPanel();
        buttons.add(add); buttons.add(update); buttons.add(delete); buttons.add(refresh);
        JPanel north = new JPanel(new BorderLayout());
        north.add(form, BorderLayout.CENTER); north.add(buttons, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add.addActionListener(e -> save(true));
        update.addActionListener(e -> save(false));
        delete.addActionListener(e -> deleteClass());
        refresh.addActionListener(e -> clear());
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && row >= 0) {
                txtMa.setText(String.valueOf(model.getValueAt(row, 0)));
                txtTen.setText(String.valueOf(model.getValueAt(row, 1)));
            }
        });
        load();
    }

    private void load() {
        model.setRowCount(0);
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT MaLop,TenLop FROM Lop ORDER BY MaLop");
             ResultSet r = p.executeQuery()) {
            while (r.next()) model.addRow(new Object[]{r.getString(1), r.getString(2)});
        } catch (Exception ex) { error(ex); }
    }

    private void save(boolean insert) {
        if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã lớp và tên lớp."); return;
        }
        String sql = insert ? "INSERT INTO Lop(MaLop,TenLop) VALUES(?,?)"
                            : "UPDATE Lop SET TenLop=? WHERE MaLop=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            if (insert) { p.setString(1, txtMa.getText().trim()); p.setString(2, txtTen.getText().trim()); }
            else { p.setString(1, txtTen.getText().trim()); p.setString(2, txtMa.getText().trim()); }
            p.executeUpdate(); load(); clear();
        } catch (Exception ex) { error(ex); }
    }

    private void deleteClass() {
        if (txtMa.getText().trim().isEmpty()) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM Lop WHERE MaLop=?")) {
            p.setString(1, txtMa.getText().trim());
            p.executeUpdate(); load(); clear();
        } catch (Exception ex) { error(ex); }
    }

    private void clear() { txtMa.setText(""); txtTen.setText(""); table.clearSelection(); }
    private void error(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
