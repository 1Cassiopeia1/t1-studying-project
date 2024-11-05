package ru.t1.java.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "data_source_error_log")
public class DataSourceErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_source_err_log_generator")
    @SequenceGenerator(name = "data_source_err_log_generator", sequenceName = "data_source_err_log_seq")
    private Long id;

    @Column(length = 1000)
    private String stacktrace;

    @Column(length = 500)
    private String message;

    @Column(nullable = false)
    private String methodSignature;

}
