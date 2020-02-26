/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import static javax.measure.unit.SI.KILOGRAM;
import javax.measure.quantity.Mass;
import org.jscience.physics.model.RelativisticModel;
import org.jscience.physics.amount.Amount;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@SpringBootApplication
public class Main {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  @RequestMapping("/")
  String index() {
    return "index";
  }

  @RequestMapping("/hello")
  String hello(Map<String, Object> model) {
      RelativisticModel.select();
      String energy = System.getenv().get("ENERGY");
      if (energy == null) {
         energy = "12 GeV";
      }
      Amount<Mass> m = Amount.valueOf(energy).to(KILOGRAM);
      model.put("science", "E=mc^2: " + energy + " = "  + m.toString());
      return "hello";
  }

  @RequestMapping("/db")
  String db(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
      stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
      stmt.execute("set search_path=salesforce, public;");
      ResultSet rs = stmt.executeQuery("SELECT name FROM staff__c");

      ArrayList<String> output = new ArrayList<String>();
      while (rs.next()) {
        output.add("Read from Staff: " + rs.getString("name"));
      }

      model.put("records", output);
      return "db";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @ModelAttribute
  public StaffForm setUpStaffForm() {
    StaffForm form = new StaffForm();
    return form;
  }

  @RequestMapping("/staff")
  String staff(Model model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      //stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
      //stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
      stmt.execute("set search_path=salesforce, public;");
      ResultSet rs = stmt.executeQuery("SELECT sfid,staffid__c,name,age__c FROM staff__c order by staffid__c desc");

      ArrayList<Staff> output = new ArrayList<Staff>();
      while (rs.next()) {
        Staff staff = new Staff();
        staff.setSfid(rs.getString("sfid"));
        staff.setStaffId(rs.getString("staffid__c"));
        staff.setName(rs.getString("name"));
        staff.setAge(rs.getString("age__c"));
        output.add(staff);
      }

      model.addAttribute("records", output);
      return "staff";
    } catch (Exception e) {
      model.addAttribute("message", e.getMessage());
      return "error";
    }
  }

//  @GetMapping("staff")
//  public String newStaff(@ModelAttribute("staff") Staff staff, Model model) {
//    try (Connection connection = dataSource.getConnection()) {
//      Statement stmt = connection.createStatement();
//      //stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
//      stmt.execute("set search_path=salesforce, public;");
//      stmt.executeUpdate("INSERT INTO staff__c (staffid__c, name, age__c) VALUES ('0007', 'Cheese', '39');");
//      //ResultSet rs = stmt.executeQuery("SELECT sfid,staffid__c,name,age__c FROM staff__c order by staffid__c desc");
//
//        //staff.setSfid(rs.getString("sfid"));
//        //staff.setStaffId(rs.getString("staffid__c"));
//        //staff.setName(rs.getString("name"));
//        //staff.setAge(rs.getString("age__c"));
//
//      //model.addAttribute("records", output);
//      return "redirect:/staff";
//    } catch (Exception e) {
//      model.addAttribute("message", e.getMessage());
//      return "error";
//    }
//  }

  @RequestMapping(value = "testform", method = RequestMethod.GET)
  public String newStaff(@RequestParam(name = "text_sfid") String sfid, 
                         @RequestParam(name = "text_flg") String flg, 
                         @RequestParam(name = "text_staffId") String staffid, 
                         @RequestParam(name = "text_name") String name,
                         @RequestParam(name = "text_age") String age,
                         Model model) {

    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();

        stmt.execute("set search_path=salesforce, public;");
        stmt.executeUpdate("INSERT INTO staff__c (staffid__c, name, age__c) VALUES ('"+ staffid +"', '"+ name +"', '"+ age +"');");

        return "redirect:/staff";

    } catch (Exception e) {
      model.addAttribute("message", e.getMessage());
      return "error";
    }
  }

  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }

}
