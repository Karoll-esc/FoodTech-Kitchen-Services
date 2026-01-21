package com.foodtech.kitchen.infrastructure.rest;

import com.foodtech.kitchen.application.usecases.CreateTableUseCase;
import com.foodtech.kitchen.application.usecases.GetAllTablesUseCase;
import com.foodtech.kitchen.application.usecases.GetTableByIdUseCase;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.infrastructure.rest.dto.CreateTableRequest;
import com.foodtech.kitchen.infrastructure.rest.dto.TableResponse;
import com.foodtech.kitchen.infrastructure.rest.mapper.TableMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tables")
public class TableController {
    
    private final CreateTableUseCase createTableUseCase;
    private final GetAllTablesUseCase getAllTablesUseCase;
    private final GetTableByIdUseCase getTableByIdUseCase;
    
    public TableController(CreateTableUseCase createTableUseCase,
                          GetAllTablesUseCase getAllTablesUseCase,
                          GetTableByIdUseCase getTableByIdUseCase) {
        this.createTableUseCase = createTableUseCase;
        this.getAllTablesUseCase = getAllTablesUseCase;
        this.getTableByIdUseCase = getTableByIdUseCase;
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TableResponse> createTable(@RequestBody CreateTableRequest request) {
        Table table = createTableUseCase.execute(request.getTableNumber(), request.getCapacity());
        TableResponse response = TableMapper.toResponse(table);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<TableResponse>> getAllTables() {
        List<Table> tables = getAllTablesUseCase.execute();
        List<TableResponse> responses = tables.stream()
            .map(TableMapper::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TableResponse> getTableById(@PathVariable Long id) {
        Table table = getTableByIdUseCase.execute(id);
        TableResponse response = TableMapper.toResponse(table);
        return ResponseEntity.ok(response);
    }
}
