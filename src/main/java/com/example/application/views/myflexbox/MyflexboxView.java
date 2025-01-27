package com.example.application.views.myflexbox;

import com.example.application.entity.Address;
import com.example.application.entity.User;
import com.example.application.repository.UserRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.util.SharedUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@PageTitle("myflexbox")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.FILE)
public class MyflexboxView extends VerticalLayout {

    @Autowired
    private UserRepository userRepository;

    public MyflexboxView() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(false);
        mainLayout.getThemeList().add("spacing-xs");

        H2 header = new H2("CSV DATA TABLE");
        // load table data from input.csv file
        Grid<String[]> grid = importTableData();

        Button saveButton = new Button("Save", event -> saveCSVData());

        mainLayout.add(header);
        mainLayout.add(grid);
        mainLayout.add(saveButton);
        add(mainLayout);
    }

    Grid<String[]> importTableData() {
        InputStreamReader csvFileReader = new InputStreamReader(
                getClass().getResourceAsStream("/input.csv"),
                StandardCharsets.UTF_8
        );

        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        CSVReader reader = new CSVReaderBuilder(csvFileReader).withCSVParser(parser).build();

        Grid<String[]> grid = new Grid<>();
        try {
            List<String[]> entries = reader.readAll();
            // Assume the first row contains headers
            String[] headers = entries.get(0);

            // Setup a grid with random data
            for (int i = 0; i < headers.length; i++) {
                final int columnIndex = i;
                String header = headers[i];
                String humanReadableHeader = SharedUtil.camelCaseToHumanFriendly(header);
                grid.addColumn(str -> str[columnIndex]).setHeader(humanReadableHeader);
            }
            grid.setItems(entries.subList(1, entries.size()));
        } catch (IOException | CsvException e) {
            grid.addColumn(nop -> "Unable to load CSV: " + e.getMessage()).setHeader("Failed to import CSV file");
        }
        return grid;
    }

    private void saveCSVData() {
        User user = new User();
        Address address = new Address();

        user.setFirstName("Arthur");
        user.setLastName("Rahman");
        address.setStreet("test street");
        address.setPostcode("8020");
        address.setCountry("Austria");
        address.setCity("Graz");
        user.setAddress(address);

        userRepository.save(user);
    }
}
