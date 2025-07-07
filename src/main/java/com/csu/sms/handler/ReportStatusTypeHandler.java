package com.csu.sms.handler;

import com.csu.sms.model.enums.ReportStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// ReportStatusTypeHandler.java
@MappedTypes(ReportStatus.class)
public class ReportStatusTypeHandler extends BaseTypeHandler<ReportStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    ReportStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public ReportStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        return fromCode(code);
    }

    @Override
    public ReportStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return fromCode(code);
    }

    @Override
    public ReportStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return fromCode(code);
    }

    private ReportStatus fromCode(int code) {
        for (ReportStatus status : ReportStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的状态码: " + code);
    }
}
