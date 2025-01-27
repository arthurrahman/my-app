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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.util.SharedUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PageTitle("myflexbox")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.FILE)
public class MyflexboxView extends VerticalLayout {

    @Autowired
    private UserRepository userRepository;
    private String[] headers;
    private Map<String, String> fieldMappings = new HashMap<>();
    private List<Map> inputData = new ArrayList<>();

    public MyflexboxView() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(false);
        mainLayout.getThemeList().add("spacing-xs");

        H2 tableHeader = new H2("CSV DATA TABLE");
        tableHeader.getStyle().set("color", "#154360");
        H2 comboBoxHeader = new H2("Map columns and save input Data");
        comboBoxHeader.getStyle().set("color", "#154360");

        // load table data from input.csv file
        Grid<String[]> dataTable = importTableData();
        
        // add combo boxes
        VerticalLayout comboBoxLayout = addComboBoxes();

        Button saveButton = new Button("Save to Database", event -> {
            saveCSVData();

            Notification notification = new Notification("Input data was saved to the database!");
            notification.setPosition(Notification.Position.TOP_END);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);;
            notification.setDuration(3000);
            notification.open();
        });
        saveButton.setWidth("600px");
        saveButton.getStyle().set("padding-top", "-10px");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        mainLayout.add(tableHeader);
        mainLayout.add(dataTable);
        mainLayout.add(comboBoxHeader);
        mainLayout.add(comboBoxLayout);
        mainLayout.add(saveButton);
        mainLayout.setSpacing(true);
        add(mainLayout);
    }

    private VerticalLayout addComboBoxes() {
        VerticalLayout verticalLayout = new VerticalLayout();
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        for (int i = 0; i < headers.length; i++) {
            final int columnIndex = i;
            ComboBox<String> comboBox = new ComboBox<>(headers[i]);
            comboBox.setItems("first", "last", "address", "zip", "country", "ignore");
            comboBox.setValue("first");
            fieldMappings.put(headers[columnIndex], "first");

            comboBox.addValueChangeListener(event -> {
                fieldMappings.put(headers[columnIndex], event.getValue());
            });

            comboBox.getStyle().set("margin-left", "-15px");
            comboBox.getStyle().set("margin-top", "-10px");
            comboBox.getStyle().set("padding-left", "-15px");
            comboBox.getStyle().set("padding-right", "10px");
            comboBox.getStyle().set("padding-top", "-10px");

            horizontalLayout.add(comboBox);

            if ((i + 1) % 3 == 0 || i == headers.length - 1) {
                verticalLayout.add(horizontalLayout);
                horizontalLayout = new HorizontalLayout();
            }
        }

        return verticalLayout;
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
            headers = entries.get(0);

            // Setup a grid with input data
            for (int i = 0; i < headers.length; i++) {
                final int columnIndex = i;
                String header = headers[i];
                String humanReadableHeader = SharedUtil.camelCaseToHumanFriendly(header);
                grid.addColumn(str -> str[columnIndex])
                        .setHeader(humanReadableHeader)
                        .setAutoWidth(true)
                        .setResizable(true);
            }
            grid.setItems(entries.subList(1, entries.size()));

            grid.setWidth("60%");
            grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

            grid.getDataProvider().fetch(new Query<>()).forEach(row -> {
                Map<String, String> inputRow = new HashMap<>();
                for (int i = 0; i < row.length; i++)
                    inputRow.put(headers[i], row[i]);

                inputData.add(inputRow);
            });
        } catch (IOException | CsvException e) {
            grid.addColumn(nop -> "Unable to load CSV: " + e.getMessage()).setHeader("Failed to import CSV file");
        }

        return grid;
    }

    private void saveCSVData() {
        for (Map<String, String> map : inputData) {
            User user = new User();
            Address address = new Address();

            user.setFirstName(map.get(fieldMappings.get("first")));
            user.setLastName(map.get(fieldMappings.get("last")));
            address.setStreet(map.get(fieldMappings.get("address")));
            address.setPostcode(map.get(fieldMappings.get("zip")));
            address.setCountry(map.get(fieldMappings.get("country")));
            user.setAddress(address);

            userRepository.save(user);
        }
    }
}
